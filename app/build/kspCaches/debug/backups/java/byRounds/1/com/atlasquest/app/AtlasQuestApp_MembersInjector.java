package com.atlasquest.app;

import com.atlasquest.app.data.local.DatabaseSeeder;
import dagger.MembersInjector;
import dagger.internal.DaggerGenerated;
import dagger.internal.InjectedFieldSignature;
import dagger.internal.QualifierMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@QualifierMetadata
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
    "cast"
})
public final class AtlasQuestApp_MembersInjector implements MembersInjector<AtlasQuestApp> {
  private final Provider<DatabaseSeeder> seederProvider;

  public AtlasQuestApp_MembersInjector(Provider<DatabaseSeeder> seederProvider) {
    this.seederProvider = seederProvider;
  }

  public static MembersInjector<AtlasQuestApp> create(Provider<DatabaseSeeder> seederProvider) {
    return new AtlasQuestApp_MembersInjector(seederProvider);
  }

  @Override
  public void injectMembers(AtlasQuestApp instance) {
    injectSeeder(instance, seederProvider.get());
  }

  @InjectedFieldSignature("com.atlasquest.app.AtlasQuestApp.seeder")
  public static void injectSeeder(AtlasQuestApp instance, DatabaseSeeder seeder) {
    instance.seeder = seeder;
  }
}
