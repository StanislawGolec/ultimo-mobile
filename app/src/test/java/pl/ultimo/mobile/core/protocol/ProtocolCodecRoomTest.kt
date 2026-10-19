package pl.ultimo.mobile.core.protocol

import com.google.common.truth.Truth.assertThat
import org.junit.Assert.assertThrows
import org.junit.Test
import pl.ultimo.mobile.core.model.RoomInfo

class ProtocolCodecRoomTest {

    @Test
    fun encodeAndDecode_createRoom_public() {
        val command = ClientCommand.CreateRoom(
            name = "ProLeague",
            visibility = "PUBLIC",
            accessCode = null,
            capacity = 8
        )
        val wire = ProtocolCodec.encodeCommand(command)
        assertThat(wire).isEqualTo("CREATE_ROOM ProLeague PUBLIC 8\n")

        val decoded = ProtocolCodec.decodeClientCommand(wire)
        assertThat(decoded).isInstanceOf(ClientCommand.CreateRoom::class.java)
        val cr = decoded as ClientCommand.CreateRoom
        assertThat(cr.name).isEqualTo("ProLeague")
        assertThat(cr.visibility).isEqualTo("PUBLIC")
        assertThat(cr.accessCode).isNull()
        assertThat(cr.capacity).isEqualTo(8)
    }

    @Test
    fun encodeAndDecode_createRoom_privateWithAccessCode() {
        val command = ClientCommand.CreateRoom(
            name = "VIP_Lounge",
            visibility = "PRIVATE",
            accessCode = "K9X2",
            capacity = 4
        )
        val wire = ProtocolCodec.encodeCommand(command)
        assertThat(wire).isEqualTo("CREATE_ROOM VIP_Lounge PRIVATE K9X2 4\n")

        val decoded = ProtocolCodec.decodeClientCommand(wire)
        assertThat(decoded).isInstanceOf(ClientCommand.CreateRoom::class.java)
        val cr = decoded as ClientCommand.CreateRoom
        assertThat(cr.name).isEqualTo("VIP_Lounge")
        assertThat(cr.visibility).isEqualTo("PRIVATE")
        assertThat(cr.accessCode).isEqualTo("K9X2")
        assertThat(cr.capacity).isEqualTo(4)
    }

    @Test
    fun encodeAndDecode_listRooms() {
        val command = ClientCommand.ListRooms
        val wire = ProtocolCodec.encodeCommand(command)
        assertThat(wire).isEqualTo("LIST_ROOMS\n")

        val decoded = ProtocolCodec.decodeClientCommand(wire)
        assertThat(decoded).isEqualTo(ClientCommand.ListRooms)
    }

    @Test
    fun encodeAndDecode_kick() {
        val command = ClientCommand.Kick("TrollUser")
        val wire = ProtocolCodec.encodeCommand(command)
        assertThat(wire).isEqualTo("KICK TrollUser\n")

        val decoded = ProtocolCodec.decodeClientCommand(wire)
        assertThat(decoded).isInstanceOf(ClientCommand.Kick::class.java)
        val kick = decoded as ClientCommand.Kick
        assertThat(kick.targetUsername).isEqualTo("TrollUser")
    }

    @Test
    fun encodeAndDecode_transferOwnership() {
        val command = ClientCommand.TransferOwnership("NewLeader")
        val wire = ProtocolCodec.encodeCommand(command)
        assertThat(wire).isEqualTo("TRANSFER_OWNERSHIP NewLeader\n")

        val decoded = ProtocolCodec.decodeClientCommand(wire)
        assertThat(decoded).isInstanceOf(ClientCommand.TransferOwnership::class.java)
        val to = decoded as ClientCommand.TransferOwnership
        assertThat(to.targetUsername).isEqualTo("NewLeader")
    }

    @Test
    fun encodeAndDecode_roomOwnerChanged() {
        val event = ServerEvent.RoomOwnerChanged("Bob")
        val wire = ProtocolCodec.encodeEvent(event)
        assertThat(wire).isEqualTo("ROOM_OWNER_CHANGED Bob\n")

        val decoded = ProtocolCodec.decodeEvent(wire)
        assertThat(decoded).isInstanceOf(ServerEvent.RoomOwnerChanged::class.java)
        val roc = decoded as ServerEvent.RoomOwnerChanged
        assertThat(roc.newOwnerUsername).isEqualTo("Bob")
    }

    @Test
    fun encodeAndDecode_roomList_nonEmpty() {
        val rooms = listOf(
            RoomInfo("room_1", "Casual Play", currentPlayers = 2, maxPlayers = 8, status = "WAITING", host = "Alice"),
            RoomInfo("room_2", "Tournament Final", currentPlayers = 4, maxPlayers = 4, status = "IN_PROGRESS", host = "Bob")
        )
        val event = ServerEvent.RoomList(rooms)
        val wire = ProtocolCodec.encodeEvent(event)
        assertThat(wire).contains("ROOM_LIST 2")
        assertThat(wire).contains("room_1:Casual_Play:2/8:WAITING:Alice")
        assertThat(wire).contains("room_2:Tournament_Final:4/4:IN_PROGRESS:Bob")

        val decoded = ProtocolCodec.decodeEvent(wire)
        assertThat(decoded).isInstanceOf(ServerEvent.RoomList::class.java)
        val rl = decoded as ServerEvent.RoomList
        assertThat(rl.rooms).hasSize(2)
        assertThat(rl.rooms[0].roomId).isEqualTo("room_1")
        assertThat(rl.rooms[0].name).isEqualTo("Casual Play")
        assertThat(rl.rooms[0].currentPlayers).isEqualTo(2)
        assertThat(rl.rooms[0].maxPlayers).isEqualTo(8)
        assertThat(rl.rooms[0].host).isEqualTo("Alice")
        assertThat(rl.rooms[1].roomId).isEqualTo("room_2")
        assertThat(rl.rooms[1].name).isEqualTo("Tournament Final")
        assertThat(rl.rooms[1].currentPlayers).isEqualTo(4)
        assertThat(rl.rooms[1].maxPlayers).isEqualTo(4)
        assertThat(rl.rooms[1].host).isEqualTo("Bob")
    }

    @Test
    fun decode_roomList_emptyTokens() {
        val decoded1 = ProtocolCodec.decodeEvent("ROOM_LIST 0\n")
        assertThat((decoded1 as ServerEvent.RoomList).rooms).isEmpty()

        val decoded2 = ProtocolCodec.decodeEvent("ROOM_LIST -\n")
        assertThat((decoded2 as ServerEvent.RoomList).rooms).isEmpty()
    }

    @Test
    fun decodeClientCommand_malformedThrows() {
        assertThrows(ProtocolException::class.java) {
            ProtocolCodec.decodeClientCommand("KICK")
        }
        assertThrows(ProtocolException::class.java) {
            ProtocolCodec.decodeClientCommand("TRANSFER_OWNERSHIP")
        }
        assertThrows(ProtocolException::class.java) {
            ProtocolCodec.decodeClientCommand("CREATE_ROOM")
        }
    }

    @Test
    fun decodeEvent_malformedThrows() {
        assertThrows(ProtocolException::class.java) {
            ProtocolCodec.decodeEvent("ROOM_OWNER_CHANGED")
        }
    }
}
