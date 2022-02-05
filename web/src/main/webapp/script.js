const domain = "https://lehsetreff.de";
// const domain = "";
// const domain = "http://localhost:8080/lehsetreff";

var threadGroups = [];
var currentUser = undefined;

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

/**
 * Legt den Cookie fuer die Session fest
 * @param name
 * Name des Cookies
 * @param value
 * Generierter String
 */
function setCookie(name, value) {
  var expires = "";
  var date = new Date();
  var days = 1;
  date.setTime(date.getTime() + days * 24 * 60 * 60 * 1000);
  expires = "; expires=" + date.toUTCString();
  document.cookie = name + "=" + (value || "") + expires + "; path=/";
}

/**
 * Liefert den Cookie zurueck
 * @param name
 * Name des Cookies
 * @returns
 * Den Cookie
 */
function getCookie(name) {
  var nameEQ = name + "=";
  var ca = document.cookie.split(";");
  for (var i = 0; i < ca.length; i++) {
    var c = ca[i];
    while (c.charAt(0) == " ") c = c.substring(1, c.length);
    if (c.indexOf(nameEQ) == 0) return c.substring(nameEQ.length, c.length);
  }
  return null;
}

$(document).ready(async function () {
  const url = $(location).attr("pathname");
  console.log(url);
  $("#testButton").click(async function () {
    var key = $("#keyInput").val();
    await getRole(key);
  });

  async function loadThreadGroups() {
    await $.ajax({
      type: "GET",
      url: domain + "/threadGroups",
      success: function (items) {
        threadGroups = items;
      },
    });
  }

  async function loadThreads() {
    $(threadGroups).each(async function (index, item) {
      await $.ajax({
        url: domain + "/threads",
        type: "GET",
        data: {
          threadGroupID: item.id,
        },
        success: function (items) {
          $(items).each(function (index, thread) {
            var threadGroupSubList = $("#tgl_" + thread.groupId);
            var listItem = document.createElement("li");
            var threadLink = document.createElement("a");
            threadLink.id = "th_" + thread.id;
            threadLink.classList.add("sub-link");
            listItem.classList.add("marginLeft10");
            threadLink.innerHTML = thread.caption;
            $(threadLink).click(function () {
              threadSelected(thread);
            });
            $(listItem).append(threadLink);
            $(threadGroupSubList).append(listItem);
          });
        },
      });
    });
  }

  function buildBreadcrumbPart(href, caption, isFinal) {
    var breadcrumb = document.createElement("li");
    breadcrumb.classList.add("breadcrumb-item");
    if (isFinal) {
      //   breadcrumb.classList.add("active");
    }
    var link = document.createElement("a");
    link.href = href;
    link.innerText = caption;
    breadcrumb.appendChild(link);
    return breadcrumb;
  }

  //   $("#applicationCaption").click(function () {
  //     console.log("home clicked");
  //     showHome();
  //   });

  function showHome() {
    $("#breadcrumb").html("");
  }

  function threadSelected(thread) {
    const group = threadGroups.find((g) => g.id == thread.groupId);
    const homePart = buildBreadcrumbPart("/", "Home", false);
    const threadGroupPart = buildBreadcrumbPart(
      "/" + group.caption,
      group.caption,
      false
    );
    const threadPart = buildBreadcrumbPart(
      "/" + group.caption + "/" + thread.caption,
      thread.caption,
      true
    );

    $("#breadcrumb").html("");
    $("#breadcrumb").append(homePart);
    $("#breadcrumb").append(threadGroupPart);
    $("#breadcrumb").append(threadPart);
  }

  function threadGroupSelected(group) {
    const homePart = buildBreadcrumbPart("/", "Home", false);
    const threadGroupPart = buildBreadcrumbPart(
      "/" + group.caption,
      group.caption,
      false
    );

    $("#breadcrumb").html("");
    $("#breadcrumb").append(homePart);
    $("#breadcrumb").append(threadGroupPart);
  }

  function listThreadGroups() {
    $(threadGroups).each(function (index, item) {
      var listItem = document.createElement("li");
      var subList = document.createElement("ul");
      $(subList).css("list-style-type", "square");
      subList.id = "tgl_" + item.id;
      var sideLink = document.createElement("a");
      sideLink.id = "tg_" + item.id;
      sideLink.classList.add("link");
      //   sideLink.classList.add("sidebar-link");
      sideLink.innerHTML = item.caption;
      $(sideLink).click(function () {
        threadGroupSelected(item);
      });
      $(listItem).append(sideLink);
      $("#sidebarThreadGroups").append(listItem);
      $("#sidebarThreadGroups").append(subList);
    });
  }

  async function getRole(apiKey) {
    await $.ajax({
      type: "GET",
      url: domain + "userRole",
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
      url: domain + "login",
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

  await loadThreadGroups();
  await loadThreads();
  listThreadGroups();
  //   showHome();
});
