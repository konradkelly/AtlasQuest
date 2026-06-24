package com.atlasquest.app.di;

import com.atlasquest.app.data.local.AppDatabase;
import com.atlasquest.app.data.local.dao.QuestionDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
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
public final class DatabaseModule_ProvideQuestionDaoFactory implements Factory<QuestionDao> {
  private final Provider<AppDatabase> dbProvider;

  public DatabaseModule_ProvideQuestionDaoFactory(Provider<AppDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public QuestionDao get() {
    return provideQuestionDao(dbProvider.get());
  }

  public static DatabaseModule_ProvideQuestionDaoFactory create(Provider<AppDatabase> dbProvider) {
    return new DatabaseModule_ProvideQuestionDaoFactory(dbProvider);
  }

  public static QuestionDao provideQuestionDao(AppDatabase db) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideQuestionDao(db));
  }
}
