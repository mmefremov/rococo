package io.efremov.rococo.page;

import static com.codeborne.selenide.Selenide.$;

import com.codeborne.selenide.SelenideElement;
import io.efremov.rococo.config.Config;
import io.efremov.rococo.model.UpdateUserInfoRequest;
import io.efremov.rococo.page.component.HeaderComponent;
import io.efremov.rococo.page.component.ToastComponent;
import io.efremov.rococo.page.modal.UserModal;
import io.qameta.allure.Step;

public abstract class BasePage<T extends BasePage<?>> {

  protected static final String FRONT_URL = Config.getInstance().frontUrl();
  protected final HeaderComponent header = new HeaderComponent();
  protected final SelenideElement self = $("#page-content");
  protected final ToastComponent toast = new ToastComponent();
  private final UserModal formModal = new UserModal();

  public T verifyPageLoaded() {
    header.shouldBeVisible();
    return (T) this;
  }

  public LoginPage clickLoginButton() {
    return header.clickLoginButton();
  }

  public T logout() {
    header.openProfileModal();
    formModal.logout();
    return (T) this;
  }

  public T updateUser(UpdateUserInfoRequest updatedUser) {
    header.openProfileModal();
    formModal.verifyUserInfo()
        .fillAllFields(updatedUser)
        .submit();
    toast.verifyAppearedMessage("Профиль обновлен");
    return (T) this;
  }

  @Step("Verify user is logged in")
  public T verifyUserIsLoggedIn() {
    header.isLoggedIn();
    return (T) this;
  }

  @Step("Verify user is not logged in")
  public T verifyUserIsNotLoggedIn() {
    header.isNotLoggedIn();
    return (T) this;
  }

  public T verifyUserInfoIsUpdated() {
    header.openProfileModal();
    formModal.verifyUserInfoIsUpdated();
    return (T) this;
  }

  @Step("Navigate to paintings page")
  public PaintingsPage goToPaintings() {
    return header.goToPaintings();
  }

  @Step("Navigate to artists page")
  public ArtistsPage goToArtists() {
    return header.goToArtists();
  }

  @Step("Navigate to museums page")
  public MuseumsPage goToMuseums() {
    return header.goToMuseums();
  }
}
