const domain = "https://lehsetreff.de";
// const domain = "http://localhost:8080/lehsetreff";

function setTheme() {
  let theme = matchMedia("(prefers-color-scheme: dark)").matches
    ? "dark"
    : "light";
  if (theme === "dark") {
    halfmoon.setDarkMode();
  } else {
    halfmoon.setLightMode();
  }
}

matchMedia("(prefers-color-scheme: dark)").addEventListener("change", () => {
  setTheme();
});

setTheme();
halfmoon.toggleSidebar();

$(document).ready(function () {
  var currentUser = undefined;

  $("#testButton").click(async function () {
    console.log("test");
    var key = $("#keyInput").val();
    console.log(key);
    await getRole(key);
  });

  async function getRole(apiKey) {
    await $.ajax({
      type: "GET",
      url: domain + "/userRole",
      data: {
        apiKey: apiKey,
      },
      beforeSend: function () {
        $("#message").text("Loading...");
      },
      success: async function (role) {
        $("#message").text("Role: " + role);
        if (role !== "Guest") {
          await login(apiKey);
          $("#keyInput").removeClass("has-Error");
        } else {
          $("#userName").text("");
          $("#keyInput").addClass("has-Error");
        }
      },
      error: function (err) {
        $("#message").text(err);
        $("#keyInput").addClass("has-Error");
      },
    });
  }

  async function login(apiKey) {
    await $.ajax({
      type: "POST",
      url: domain + "/login",
      data: {
        apiKey: apiKey,
      },
      beforeSend: function () {
        $("#userName").text("Loading...");
      },
      success: async function (user) {
        currentUser = user;
        $("#userName").text("Name: " + user.userName + "!");
      },
      error: function (err) {
        $("#userName").text("Error loading userName");
      },
    });
  }
});
