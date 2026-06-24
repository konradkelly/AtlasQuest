package com.atlasquest.app.data.local;

import android.content.Context;
import com.atlasquest.app.data.local.dao.QuestionDao;
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
    "cast"
})
public final class DatabaseSeeder_Factory implements Factory<DatabaseSeeder> {
  private final Provider<Context> contextProvider;

  private final Provider<QuestionDao> daoProvider;

  public DatabaseSeeder_Factory(Provider<Context> contextProvider,
      Provider<QuestionDao> daoProvider) {
    this.contextProvider = contextProvider;
    this.daoProvider = daoProvider;
  }

  @Override
  public DatabaseSeeder get() {
    return newInstance(contextProvider.get(), daoProvider.get());
  }

  public static DatabaseSeeder_Factory create(Provider<Context> contextProvider,
      Provider<QuestionDao> daoProvider) {
    return new DatabaseSeeder_Factory(contextProvider, daoProvider);
  }

  public static DatabaseSeeder newInstance(Context context, QuestionDao dao) {
    return new DatabaseSeeder(context, dao);
  }
}
