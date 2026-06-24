package com.atlasquest.app.data.repository;

import com.atlasquest.app.data.local.dao.QuestionDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
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
public final class QuestionRepository_Factory implements Factory<QuestionRepository> {
  private final Provider<QuestionDao> daoProvider;

  public QuestionRepository_Factory(Provider<QuestionDao> daoProvider) {
    this.daoProvider = daoProvider;
  }

  @Override
  public QuestionRepository get() {
    return newInstance(daoProvider.get());
  }

  public static QuestionRepository_Factory create(Provider<QuestionDao> daoProvider) {
    return new QuestionRepository_Factory(daoProvider);
  }

  public static QuestionRepository newInstance(QuestionDao dao) {
    return new QuestionRepository(dao);
  }
}
