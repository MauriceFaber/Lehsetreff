const domain = "https://lehsetreff.de";
// const domain = "";
// const domain = "http://localhost:8080/lehsetreff";

var threads = [];
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
  var url = $(location).attr("pathname");

  $("#homeView").hide();
  $("#threadView").hide();
  $("#threadGroupView").hide();

  $("#testButton").click(async function () {
    var key = $("#keyInput").val();
    await getRole(key);
  });

  async function loadThreadGroups() {
    return await $.ajax({
      type: "GET",
      url: domain + "/threadGroups",
      success: async function (items) {
        threadGroups = items;
        listThreadGroups();
        $(threadGroups).each(async function (index, threadGroup) {
          await loadThreads(threadGroup);
          listThreads(threadGroup);
        });
      },
    });
  }

  function showHomeContent() {
    $("#threadView").hide();
    $("#threadGroupView").hide();
    $("#homeView").show();
  }

  function showThreadContent() {
    $("#threadGroupView").hide();
    $("#homeView").hide();
    $("#threadView").show();
  }

  function showThreadGroupContent() {
    $("#threadView").hide();
    $("#homeView").hide();
    $("#threadGroupView").show();
  }

  function loadContent(href, isInitial) {
    if (href === "/" || href == "/index.html") {
      var homePart = buildBreadcrumbPart("/", "Home", false);
      $("#breadcrumb").html("");
      $("#breadcrumb").append(homePart);
      showHomeContent();
    } else {
      var count = countCharInString("/", href);
      if (count === 1) {
        if (isInitial) {
          const caption = href.substring(1);
          const group = threadGroups.find((g) => g.caption === caption);
          threadGroupSelected(group);
        }
        showThreadGroupContent();
      } else {
        if (isInitial) {
          const caption = href.split("/")[2];
          const thread = threads.find((t) => t.caption === caption);
          if (!thread) {
            return false;
          }
          threadSelected(thread);
        }
        showThreadContent();
      }
    }
    return true;
  }

  async function loadThreads(threadGroup) {
    await $.ajax({
      url: domain + "/threads",
      type: "GET",
      data: {
        threadGroupID: threadGroup.id,
      },
      success: function (items) {
        threadGroup.threads = items;
        threads = threads.concat(items);
      },
    });
  }

  function linkClicked(href) {
    history.pushState({}, null, href);
    url = href;
  }

  function countCharInString(char, string) {
    var count = 0;
    for (var i = 0; i < string.length; i++) {
      if (char === string[i]) {
        count++;
      }
    }
    return count;
  }

  function refreshBreadCrumb(href) {
    var oldCount = countCharInString("/", url);
    var delimiterCount = countCharInString("/", href);
    if (url === "/") {
      oldCount = 0;
    }
    if (href === "/") {
      delimiterCount = 0;
    }
    const delimiterToRemove = oldCount - delimiterCount;

    for (var i = 0; i < delimiterToRemove; i++) {
      $("#breadcrumb").children().last().remove();
    }
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
    $(link).click(function (e) {
      refreshBreadCrumb(href);
      linkClicked(href);
      loadContent(href, false);
      return false;
    });
    breadcrumb.appendChild(link);
    return breadcrumb;
  }

  $("#applicationCaption").click(function () {
    showHome();
  });

  function showHome() {
    loadContent("/", false);
  }

  function threadSelected(thread) {
    const group = threadGroups.find((g) => g.id === thread.groupId);
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

    const href = "/" + group.caption + "/" + thread.caption;
    linkClicked(href);
    loadContent(href, false);
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

    const href = "/" + group.caption;
    linkClicked(href);
    loadContent(href, false);
  }

  function listThreadGroups() {
    threadGroups.forEach((item) => {
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
        halfmoon.toggleSidebar();
      });
      $(listItem).append(sideLink);
      $("#sidebarThreadGroups").append(listItem);
      $("#sidebarThreadGroups").append(subList);
    });
  }

  function listThreads(threadGroup) {
    $(threadGroup.threads).each(function (index, thread) {
      var threadGroupSubList = $("#tgl_" + thread.groupId);
      var listItem = document.createElement("li");
      var threadLink = document.createElement("a");
      threadLink.id = "th_" + thread.id;
      threadLink.classList.add("sub-link");
      listItem.classList.add("marginLeft10");
      threadLink.innerHTML = thread.caption;
      $(threadLink).click(function () {
        threadSelected(thread);
        halfmoon.toggleSidebar();
      });
      $(listItem).append(threadLink);
      $(threadGroupSubList).append(listItem);
    });
  }

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

  function isInitialLoaded() {
    var allThreadsLoaded = true;
    threadGroups.forEach((g) => {
      allThreadsLoaded &= g.threads !== undefined;
    });
    return allThreadsLoaded;
  }

  await loadThreadGroups();
  waitForElement();

  function waitForElement() {
    if (isInitialLoaded()) {
      if (!loadContent(url, true)) {
        setTimeout(waitForElement, 100);
      }
    } else {
      setTimeout(waitForElement, 100);
    }
  }
});
