package pl.ultimo.mobile.core.network;

import android.content.Context;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast",
    "deprecation"
})
public final class AndroidNetworkConnectivityObserver_Factory implements Factory<AndroidNetworkConnectivityObserver> {
  private final Provider<Context> contextProvider;

  public AndroidNetworkConnectivityObserver_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public AndroidNetworkConnectivityObserver get() {
    return newInstance(contextProvider.get());
  }

  public static AndroidNetworkConnectivityObserver_Factory create(
      Provider<Context> contextProvider) {
    return new AndroidNetworkConnectivityObserver_Factory(contextProvider);
  }

  public static AndroidNetworkConnectivityObserver newInstance(Context context) {
    return new AndroidNetworkConnectivityObserver(context);
  }
}
