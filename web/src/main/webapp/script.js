function replaceURLs(message) {
  if (!message) return;

  var urlRegex = /(((https?:\/\/)|(www\.))[^\s]+)/g;
  return message.replace(urlRegex, function (url) {
    var hyperlink = url;
    if (!hyperlink.match("^https?://")) {
      hyperlink = "http://" + hyperlink;
    }
    return (
      '<a href="' +
      hyperlink +
      '" target="_blank" rel="noopener noreferrer">' +
      url +
      "</a>"
    );
  });
}

var mini = false;

$(document).ready(function () {
  toggleSidebar();
  document.addEventListener("touchmove", function () {
    document.body.scrollTop = 0;
  });

  var currentUser = null;
  var currentChatroomID = undefined;
  var refreshIntervalId;
  const intervalMs = 2000;

  var currentChatrooms = [];
  var currentContacts = [];

  const groupChat = 0;
  const singleChat = 1;

  const messageType = 0;
  const imageType = 1;

  //const domain = "http://localhost:8080/meshenger";
  const domain = "https://meshenger.de";
  //const domain = "";

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

  /**
   * Liefert eingegebene Datei (Avatar) als Base64 zurueck
   * @param file
   * Ausgewaehlte Datei
   * @returns
   * Datei mit Format Base64
   */
  const toBase64 = (file) =>
    new Promise((resolve, reject) => {
      const reader = new FileReader();
      reader.readAsDataURL(file);
      reader.onloadend = () => resolve(reader.result);
      reader.onerror = (error) => reject(error);
    });

  /**
   * Aktualisiert den Benutzeravatar mit dem Base64 Image
   * @param imagePath
   * Pfad des Avatars
   * @returns
   * Aktualisierter Avatar
   */
  async function updateAvatar(imagePath) {
    var base64 = await toBase64(imagePath);
    return await putAvatar(base64);
  }

  /**
   * Laedt den Avatar in die Datenbank
   * @param base64
   * Ausgewaehler Avatar
   * @returns
   * Ergebnis mit true oder false
   */
  async function putAvatar(base64) {
    var result = false;
    await $.ajax({
      type: "PUT",
      url: domain + "/users",
      crossDomain: true,
      data: {
        apiKey: currentUser.apiKey,
        avatar: base64,
      },
      beforeSend: function () {},
      success: async function (user) {
        user.apiKey = currentUser.apiKey;
        await onUserLoginSuccess(user);
        result = true;
      },
      error: function (html) {
        var base64 = toBase64(
          "https://pbs.twimg.com/profile_images/746460305396371456/4QYRblQD.jpg"
        );
        setAvatar(base64);
      },
    });
    return result;
  }

  /**
   * Entfernt gesetzen Cookie
   * @param name
   * Name des Cookies
   */
  function eraseCookie(name) {
    document.cookie =
      name + "=; Path=/; Expires=Thu, 01 Jan 1970 00:00:01 GMT;";
  }

  /**
   * Funktion zum Senden einer Nachricht
   * @param content
   * Inhalt der Nachricht
   * @param chatroomId
   * Id des betroffenen Chatrooms
   */
  async function sendMessage(content, contentType, chatroomId) {
    await $.ajax({
      type: "POST",
      url: domain + "/messages",
      crossDomain: true,
      data: {
        apiKey: currentUser.apiKey,
        chatroomId: chatroomId,
        content: content,
        contentType: contentType,
      },
      beforeSend: function () {},
      success: function (item) {
        $("#messagesinnercontainer").append(createMessageDiv(item));
        showMessages();
        scrollToBottom();
      },
      error: function (html) {},
    });
  }

  /**
   * Funktion zum Abrufen der Nachrichten fuer spezifischen Chatroom
   * @param chatroomId
   * Id des betroffenen Chatrooms
   * @returns
   * Ergebnis mit true oder false
   */
  async function getMessages(chatroomId) {
    var result = false;
    var room = await getChatroomFromCurrent(chatroomId);
    if (!room.messages) {
      room.messages = [];
      room = await getChatroomFromCurrent(chatroomId);
    }

    await $.ajax({
      type: "GET",
      url: domain + "/messages",
      crossDomain: true,
      data: {
        apiKey: currentUser.apiKey,
        chatroomId: chatroomId,
      },
      beforeSend: function () {},
      success: function (html) {
        if (html.length === 0) {
          $("#messagesLoader").hide();
        }
        $(html).each(function (index, item) {
          $("#messagesLoader").hide();
          if (!room.messages.find((m) => m.id === item.id)) {
            room.messages.push(item);
          }
          if (
            $("#m_" + item.id).length === 0 &&
            room.id === currentChatroomID
          ) {
            $("#messagesinnercontainer").append(createMessageDiv(item));
            result = true;
          }
        });
      },
    });
    return result;
  }

  /**
   * Erzeugt neues Div-Element fuer Nachricht
   * @param message
   * Die betroffene Nachricht
   * @returns
   * das Div Element
   */
  function createMessageDiv(message) {
    var newdiv = document.createElement("div");
    newdiv.id = "m_" + message.id;
    var received = message.senderId === currentUser.id;
    newdiv.className = "chatcontainer msg msgGlass";
    if (received) newdiv.className += " right";

    var span = document.createElement("span");
    span.innerHTML = "<b>" + message.senderName + "</b>";
    newdiv.appendChild(span);

    var room = currentChatrooms.find((c) => c.id === currentChatroomID);
    if (room) {
      var member = room.members.find((m) => m.id === message.senderId);
      if (member) {
        var avatar = document.createElement("img");
        avatar.src = member.avatar;
        newdiv.appendChild(avatar);
      }
    }

    var br = document.createElement("br");
    newdiv.appendChild(br);

    if (message.contentType === messageType) {
      span = document.createElement("span");
      //url als link darstellen
      var text = replaceURLs(message.content);
      span.innerHTML = text;
      newdiv.appendChild(span);
    } else if (message.contentType === imageType) {
      var img = document.createElement("img");
      img.className = "imageMessage";
      img.src = message.content;
      img.alt = "Bild nicht gefunden.";

      $(img).click(function () {
        $("#popUpImage").attr("src", this.src);
        $("#imageModal").modal("show");
      });

      newdiv.appendChild(img);
    }

    span = document.createElement("span");
    span.className = "time-right";

    span.textContent = toLocalTimeString(new Date(message.timeStamp));
    newdiv.appendChild(span);

    return newdiv;
  }

  function toLocalTimeString(utcDate) {
    utcDate.setMonth(utcDate.getMonth() + 1);
    utcDate.setTime(utcDate.getTime() + 1 * 60 * 60 * 1000);
    var month = utcDate.getMonth();
    var day = utcDate.getDate();
    var hour = utcDate.getHours();
    var min = utcDate.getMinutes();
    var sec = utcDate.getSeconds();

    month = (month < 10 ? "0" : "") + month;
    day = (day < 10 ? "0" : "") + day;
    hour = (hour < 10 ? "0" : "") + hour;
    min = (min < 10 ? "0" : "") + min;
    sec = (sec < 10 ? "0" : "") + sec;

    var str =
      day +
      "." +
      month +
      "." +
      utcDate.getFullYear() +
      " " +
      hour +
      ":" +
      min +
      ":" +
      sec;

    return str;
  }

  /**
   * Erstellt Chatroom mit entsprechendem Namen und Avatar
   * @param chatName
   * Name, den der Chatroom bekommen soll
   */
  async function createChatroom(chatName) {
    await $.ajax({
      type: "POST",
      url: domain + "/chatrooms",
      crossDomain: true,
      data: {
        apiKey: currentUser.apiKey,
        chatName: chatName,
        chatroomType: groupChat,
      },
      success: async function (chatroom) {
        await getChatrooms();
        await selectChatroom(chatroom.id);
      },
    });
  }

  /**
   * Funktion die den Avatar des Chatrooms zurueckliefert
   * @param  id
   * Der betroffene Chatroom
   * @returns
   * Chatroom Avatar
   */
  async function getChatroomAvatar(id) {
    var chatroom = await getChatroomFromCurrent(id);
    var result =
      "https://pbs.twimg.com/profile_images/746460305396371456/4QYRblQD.jpg";
    if (
      chatroom &&
      chatroom.avatar !== undefined &&
      chatroom.avatar !== "default"
    ) {
      result = chatroom.avatar;
    }
    return result;
  }

  /**
   * Funktion die den ausgewaehlten Chatroom zurueckliefert
   * @param id
   * Id des betroffenen Chatrooms
   * @returns
   * Der Chatroom
   */
  async function getChatroom(id) {
    var result = undefined;
    await $.ajax({
      type: "GET",
      url: domain + "/chatrooms",
      crossDomain: true,
      data: {
        apiKey: currentUser.apiKey,
        chatroomId: id,
      },
      success: async function (chatroom) {
        if (chatroom && chatroom.members) {
          chatroom.members.forEach((m) => {
            if (!m.avatar || m.avatar === "default") {
              m.avatar =
                "https://pbs.twimg.com/profile_images/746460305396371456/4QYRblQD.jpg";
            }
          });
          result = chatroom;
        }
      },
    });
    return result;
  }

  var refreshing = false;
  function disableRefresh() {
    refreshing = false;
    clearInterval(refreshIntervalId);
  }

  function enableRefresh() {
    refreshing = true;
    refreshIntervalId = setInterval(refreshMessages, intervalMs);
  }

  /**
   * Loggt den User aus der Session aus und loescht den Cookie
   */
  function logout() {
    currentUser = null;
    currentChatroomID = null;
    disableRefresh();
    $("#mainContent").hide();
    $("#loginModal").show();
    $("#memberinfo").html("...");
    eraseCookie("apiKey");
    $("#contactsList").html("");
    $("#chatroomscontainer").html("");
    $("#messagesinnercontainer .msgGlass").remove();
  }

  async function setChatroom(chatroom) {
    if (!currentChatroomID || chatroom.id !== currentChatroomID) {
      disableRefresh();
      $("#ch_" + chatroom.id).addClass("active");
      $("#ch_b_" + chatroom.id).text("");
      deleteNotification();

      $("#messagesinnercontainer").html("");

      currentChatroomID = chatroom.id;

      $("#chatroomTitle").html(chatroom.name);
      $("#chatroomInfo").html("");
      await OnChatroomInfoVisible(chatroom.id, false);
      enableRefresh();
      await refreshMessages();
    }
    showMessages();
  }

  /**
   * Funktion um Chatrooms aus der Datebank abzurufen und in HTML zu laden
   */
  async function getChatrooms() {
    var result = undefined;
    await $.ajax({
      type: "GET",
      url: domain + "/chatrooms",
      crossDomain: true,
      data: {
        apiKey: currentUser.apiKey,
      },
      success: function (chatrooms) {
        result = chatrooms;
      },
    });
    if (!currentChatrooms) {
      currentChatrooms = [];
    }
    $(".chatroom").each(async function (index, item) {
      var id = parseInt(item.id.substring(3));
      var tmp = result.find((c) => c.id === id);

      if (!tmp) {
        $(item).remove();
        var index = currentChatrooms.indexOf(item);
        currentChatrooms.splice(index, 1);
      }
    });
    if (result) {
      $(result).each(async function () {
        var r = currentChatrooms.find((ch) => ch.id === this.id);
        if (!r) {
          currentChatrooms.push(this);
        } else {
          await setDisplayName(this);
          r.name = this.name;
          r.latestMessage = this.latestMessage;
          r.latestUserMessage = this.latestUserMessage;
        }
      });
    }
    listChatrooms();
  }

  /**
   * Funktion zum erstellen des Chatroom Div
   * @param item
   * Der betroffene Chatroom
   * @returns
   * Den Div-Element
   */
  async function createChatroomDiv(item) {
    var newli = document.createElement("li");
    newli.className = "list-group-item list-group-item-action chatroom glass";

    newli.id = "ch_" + item.id;

    var newimg = document.createElement("img");

    var avatar = await getChatroomAvatar(item.id);
    item = await getChatroomFromCurrent(item.id);
    // var isSingleChat = item.members.length === 2;
    // item.members.forEach(
    //   (m) => (isSingleChat &&= item.name.includes(m.userName))
    // );
    newimg.src = avatar;

    var trimmedName = item.name;
    var newspan = document.createElement("span");
    newspan.id = "ch_n_" + item.id;

    const maxLength = 20;
    if (trimmedName.length > maxLength) {
      trimmedName = trimmedName.substring(0, maxLength - 3) + "...";
    }
    newspan.textContent = trimmedName;

    var newbadge = document.createElement("span");
    newbadge.className = "badge badge-pill badge-info pull-right";

    newbadge.id = "ch_b_" + item.id;

    newli.appendChild(newimg);
    newli.appendChild(newspan);

    newli.appendChild(newbadge);

    return newli;
  }

  /**
   * Funktion die eine Infobox fuer einen Chatroom erstellt
   * @param item
   * Der betroffene Chatroom
   * @returns
   * Das Div-Element
   */
  async function createChatroomInfoDiv(item) {
    var newli = document.createElement("div");
    newli.className = "chatroomInfo";

    var newimg = document.createElement("img");

    var avatar = await getChatroomAvatar(item.id);

    newimg.src = avatar;
    var newspan = document.createElement("span");
    if (item.chatroomType === groupChat) {
      newspan.className = "nameSpan";
    }
    var tmpName = item.name;
    if (item.chatroomType === singleChat) {
      tmpName = "Chat mit " + tmpName;
    }
    newspan.textContent = tmpName;

    var changeNameButton = document.createElement("button");
    changeNameButton.className = "btn";
    changeNameButton.innerText = "Name aendern";
    $(changeNameButton).hide();

    $(changeNameButton).attr("data-toggle", "modal");
    $(changeNameButton).attr("data-target", "#changeChatroomName");

    $(changeNameButton).click(function () {
      var currentChatroom = currentChatrooms.find(
        (c) => c.id === currentChatroomID
      );
      $("#changeChatNameInput").val(currentChatroom.name);
    });

    if (item.chatroomType === groupChat) {
      $(newspan).click(function () {
        $(changeNameButton).trigger("click");
      });

      newimg.className = "chatImg";
      var avatarSelector = document.createElement("input");
      avatarSelector.type = "file";
      avatarSelector.className = "btn";
      $(avatarSelector).hide();

      $(avatarSelector).on("change", async function () {
        var image = await toBase64(this.files[0]);
        await putChatroomAvatar(image);
      });
    }

    $(newimg).click(function () {
      $(avatarSelector).trigger("click");
    });

    var deleteChatroomButton = document.createElement("button");
    deleteChatroomButton.className = "btn float-right";

    var deleteIcon = document.createElement("i");
    deleteIcon.className = "fa fa-times";
    deleteChatroomButton.append(deleteIcon);

    $(deleteChatroomButton).click(async function () {
      await deleteChatroom();
    });

    var addButton = document.createElement("button");
    addButton.className = "btn addButton float-right";

    var currentChatroom = currentChatrooms.find(
      (c) => c.id === currentChatroomID
    );
    $(addButton).click(function () {
      $("#contactsToAddList").html("");

      $(currentContacts).each(function (index, member) {
        var test = currentChatroom.members.find((m) => m.id === member.id);
        if (!test) {
          $("#contactsToAddList").append(createContactToAddElement(member));
        }
      });
    });

    $(addButton).attr("data-toggle", "modal");
    $(addButton).attr("data-target", "#addUserToChat");

    var plusIcon = document.createElement("i");
    plusIcon.className = "fa fa-plus";
    addButton.append(plusIcon);

    var leaveChatroomButton = document.createElement("button");
    leaveChatroomButton.className = "btn float-right";

    var leaveIcon = document.createElement("i");
    leaveIcon.className = "fa fa-minus";
    leaveChatroomButton.append(leaveIcon);

    $(leaveChatroomButton).click(async function () {
      await removeContactFromChatroom(currentChatroomID, currentUser.id);
    });

    var addContactButton = document.createElement("button");
    addContactButton.className = "btn float-right";

    var addContactIcon = document.createElement("i");
    addContactIcon.className = "fa fa-address-book";
    addContactButton.append(addContactIcon);

    var addAddContactButton = false;
    if (item.chatroomType === singleChat) {
      var user = currentChatroom.members.find((m) => m.id !== currentUser.id);
      addAddContactButton = !currentContacts.find((c) => c.id === user.id);

      $(addContactButton).click(async function () {
        await addContact(user.userName, user.id);
      });
    }

    var usersHeader = document.createElement("h5");
    usersHeader.innerHTML = "Users";

    var userList = document.createElement("ul");
    userList.className = "list-group";
    var chatroomId = item.id;
    $(item.members).each(function (index, item) {
      var contactDiv = createRawContactDiv(item, chatroomId);
      userList.appendChild(contactDiv);
    });

    newli.append(newimg);
    newli.append(newspan);
    if (item.chatroomType === groupChat) {
      newli.append(changeNameButton);
      newli.append(avatarSelector);
      newli.append(deleteChatroomButton);
      newli.append(addButton);
      newli.append(usersHeader);
      newli.append(userList);
    } else {
      if (addAddContactButton) {
        newli.append(addContactButton);
      }
      newli.append(leaveChatroomButton);
    }
    return newli;
  }

  $("#changeChatNameButton").click(async function () {
    await putChatroomName($("#changeChatNameInput").val());
  });

  /**
   * Funktion zum loeschen eines Chatrooms
   */
  async function deleteChatroom() {
    await $.ajax({
      type: "DELETE",
      url: domain + "/chatrooms",
      crossDomain: true,
      data: {
        apiKey: currentUser.apiKey,
        chatroomId: currentChatroomID,
      },
      success: function () {
        location.reload(true);
      },
    });
  }

  /**
   * Funktion zum Aktualisieren des Avatars
   * @param image
   * Der neue Avatar
   */
  async function putChatroomAvatar(image) {
    await $.ajax({
      type: "PUT",
      url: domain + "/chatrooms",
      crossDomain: true,
      data: {
        apiKey: currentUser.apiKey,
        chatroomId: currentChatroomID,
        avatar: image,
      },
      success: function () {
        location.reload(true);
      },
    });
  }

  /**
   * Funktion zum Aktualisieren des Chatroom Namens
   * @param name
   * Der neue Name
   */
  async function putChatroomName(name) {
    await $.ajax({
      type: "PUT",
      url: domain + "/chatrooms",
      crossDomain: true,
      data: {
        apiKey: currentUser.apiKey,
        chatroomId: currentChatroomID,
        name: name,
      },
      success: function () {
        //location.reload(true);
      },
    });
  }

  /**
   *  Kontakte werden abgerufen und in View geladen
   */
  async function getContacts() {
    await $.ajax({
      type: "GET",
      url: domain + "/contacts",
      crossDomain: true,
      data: {
        apiKey: currentUser.apiKey,
      },
      beforeSend: function () {},
      success: function (contacts) {
        currentContacts = contacts;
        $("#contactsList").html("");
        $(contacts).each(function (index, item) {
          $("#contactsList").append(createContactElement(item));
        });
        return contacts;
      },
      error: function (html) {
        return null;
      },
    });
  }

  /**
   * Erstellt ListElement fuer den Kontakt
   * @param item
   * Der betroffene Kontakt
   * @returns
   * Das List Element
   */
  function createContactElement(item) {
    var newli = document.createElement("li");
    newli.classList.add("list-group-item");
    newli.classList.add("contact");
    newli.classList.add("rounded");

    newli.classList.add("justify-content-between");
    newli.classList.add("align-items-center");
    newli.id = "c_" + item.id;

    var newimg = document.createElement("img");
    if (item.avatar && item.avatar != "default") {
      newimg.src = item.avatar;
    } else {
      newimg.src =
        "https://pbs.twimg.com/profile_images/746460305396371456/4QYRblQD.jpg";
    }

    newli.append(newimg);

    newli.append(item.userName + " " + "(" + item.id + ")");

    var startChatbtn = document.createElement("button");
    startChatbtn.className = "btn float-right";
    startChatbtn.id = "startChatButton_" + item.id;
    startChatbtn.type = "button";
    $(startChatbtn).click(async function () {
      var chatName = item.userName + "-" + currentUser.userName;
      await startChat(item.id, chatName);
    });

    var startChatIcon = document.createElement("i");
    startChatIcon.className = "fa fa-comment";
    startChatIcon.setAttribute("aria-hidden", "true");
    startChatbtn.appendChild(startChatIcon);

    var removeButton = document.createElement("button");
    removeButton.className = "btn float-right";
    removeButton.id = "deleteContactButton_" + item.id;
    removeButton.type = "button";
    $(removeButton).click(async function () {
      await removeContact(item.id);
    });

    var newi = document.createElement("i");
    newi.className = "fa fa-user-times";
    newi.setAttribute("aria-hidden", "true");
    removeButton.appendChild(newi);

    newli.appendChild(removeButton);
    newli.appendChild(startChatbtn);

    return newli;
  }

  /**
   * Funktion um einen Chat mit einem Kontakt zu starten
   * @param contactId
   * Der betroffene Kontakt
   * @param chatName
   * Name des Chatrooms
   */
  async function startChat(contactId, chatName) {
    var existingChat = currentChatrooms.find(
      (c) =>
        c.chatroomType === singleChat &&
        c.members.find((m) => m.id === contactId)
    );
    if (existingChat) {
      await selectChatroom(existingChat.id);
      $("#showContacts").modal("hide");
      return;
    }
    await $.ajax({
      type: "POST",
      url: domain + "/chatrooms",
      crossDomain: true,
      data: {
        apiKey: currentUser.apiKey,
        chatName: chatName,
        chatroomType: singleChat,
      },
      beforeSend: function () {},
      success: async function (chatroom) {
        await addContactsToChatroom(chatroom.id, [contactId]);
        await getChatrooms();
        await selectChatroom(chatroom.id);
        $("#showContacts").modal("hide");
      },
    });
  }

  /**
   * Funktion die ein Listenelement fuer einen Kontakt erzeugt und es zurueck liefert
   * @param  item
   * Betroffener Kontakt
   * @param  chatroomId
   * Id des Chatrooms
   * @returns
   * Kontakt als List Element
   */
  function createRawContactDiv(item, chatroomId) {
    var newli = document.createElement("li");
    newli.classList.add("list-group-item");
    newli.classList.add("contact");
    newli.classList.add("justify-content-between");
    newli.classList.add("align-items-center");

    var newimg = document.createElement("img");
    if (item.avatar && item.avatar != "default") {
      newimg.src = item.avatar;
    } else {
      newimg.src =
        "https://pbs.twimg.com/profile_images/746460305396371456/4QYRblQD.jpg";
    }

    newli.append(newimg);

    newli.append(item.userName + " " + "(" + item.id + ")");

    var removeButton = document.createElement("button");
    removeButton.className = "btn removeButton float-right";

    var removeIcon = document.createElement("i");
    removeIcon.className = "fa fa-minus";
    removeButton.append(removeIcon);

    $(removeButton).click(async function () {
      await removeContactFromChatroom(chatroomId, item.id);
    });

    newli.append(removeButton);
    return newli;
  }

  /**
   * Fuegt einen Kontakt der Kontaktliste hinzu und laedt die Kontakte
   * @param  name
   * Name des Kontakts
   * @param  id
   * Id des Kontakts
   */
  async function addContact(name, id) {
    await $.ajax({
      type: "POST",
      url: domain + "/contacts",
      crossDomain: true,
      data: {
        apiKey: currentUser.apiKey,
        contactName: name,
        contactId: id,
      },
      beforeSend: function () {},
      success: async function (contacts) {
        $("#addNewContact").modal("hide");
        await loadContacts();
      },
    });
  }

  /**
   * Entfernt den Kontakt aus der Kontaktliste und laedt die Kontakte
   * @param contactId
   * Id des Kontakts
   */
  async function removeContact(contactId) {
    await $.ajax({
      type: "DELETE",
      url: domain + "/contacts",
      crossDomain: true,
      data: {
        apiKey: currentUser.apiKey,
        contactId: contactId,
      },
      beforeSend: function () {},
      success: function () {
        loadContacts();
      },
    });
  }

  /**
   * Laedt die Chatrooms
   */
  async function loadChatrooms() {
    if (currentUser) {
      await getChatrooms();
    }
  }

  /**
   * Laedt die Kontakte
   */
  async function loadContacts() {
    if (currentUser !== null) {
      await getContacts(currentUser.apiKey);
    }
  }

  /**
   * Klick auf Senden-Button startet sendMessage Funktion
   */
  $("#sendMessageButton").click(async function () {
    var content = $("#message").val();
    $("#message").val("");
    await sendMessage(content, messageType, currentChatroomID);
  });

  /**
   * Klick auf Login-Button startet Login Funktion
   */
  $("#loginButton").click(function () {
    loginWithInputs();
  });

  /**
   * Login mit den Daten aus den entsprechenden HTMl Eingabefeldern
   */
  async function loginWithInputs() {
    var name = $("#userNameInput").val();
    var pwd = $("#passwordInput").val();
    if (name.length != 0 && pwd.length != 0) {
      await login(name, pwd);
    }
  }

  /**
   * Login mit dem entsprechendem ApiKey
   * @param apiKey
   * ApiKey des Benutzers
   */
  async function loginWithApiKey(apiKey) {
    await loginWithKey(apiKey);
  }

  /**
   * Funkion die Kontakte in einem Chatroom zurueckliefert
   * @param chatroomId
   * Der betroffene Chatroom
   * @param withAvatars
   * Die Avatare der Kontakte
   * @returns
   * Die Kontakte des Chatrooms
   */
  async function getMembers(chatroomId, withAvatars) {
    var result = [];
    await $.ajax({
      type: "GET",
      url: domain + "/members",
      crossDomain: true,
      data: {
        apiKey: currentUser.apiKey,
        chatroomId: chatroomId,
        withAvatars: withAvatars,
      },
      success: function (members) {
        result = members;
      },
    });
    return result;
  }

  /**
   * Funktion die den Chatroom Namen anzeigt
   * @param chat
   * Der betroffene Chatroom
   */
  async function setDisplayName(chat) {
    if (chat.chatroomType === singleChat) {
      if (!chat.members || chat.members.length === 0) {
        chat.members = await getMembers(chat.id, false);
      }
      var otherUser = chat.members.find((m) => m.id !== currentUser.id);
      if (otherUser !== undefined) {
        chat.avatar = otherUser.avatar;
        chat.name = otherUser.userName;
      } else {
        chat.avatar = "default";
        chat.name = "geloeschter Chat";
      }
    }
  }

  /**
   * Funktion die einen Chatroom laedt und fuellt
   * @param id
   * Id des betroffenen Chatrooms
   * @returns
   * Den Chatroom
   */
  async function getChatroomFromCurrent(id) {
    var result = currentChatrooms.find((c) => c.id === id);
    if (!result || result.members.length === 0) {
      const index = currentChatrooms.indexOf(result);
      if (index > -1) {
        currentChatrooms.splice(index, 1);
      }

      result = await getChatroom(id);
      if (result) {
        await setDisplayName(result);
        currentChatrooms.push(result);
      }
    }
    return result;
  }

  /**
   * Funktion die aufgerufen wird, wenn man die Info des Chatrooms oeffnen will
   * @param id
   * Der betroffene Chatroom
   * @param forceReload
   * Variable, um den das Objekt zu aktualisieren
   */
  async function OnChatroomInfoVisible(id, forceReload) {
    if (forceReload) {
      var index = currentChatrooms.indexOf(
        currentChatrooms.find((c) => c.id === id)
      );
      currentChatrooms.splice(index, 1);
    }
    var chatroom = await getChatroomFromCurrent(id);
    var div = await createChatroomInfoDiv(chatroom);
    $("#chatroomInfo").html(div);
  }

  $("#registerButton").click(function () {
    var name = $("#userNameInputReg").val();
    var pwd = $("#passwordInputReg").val();
    var avatar = $("#avatarFile").prop("files")[0];
    if (name.length != 0 && pwd.length != 0 && avatar.length != 0) {
      $("#loginModal").modal("hide");
      register(name, pwd, avatar);
    }
  });

  $("#logoutButton").click(function () {
    logout();
  });

  // $("#getChatsButton").click(function () {
  //   loadChatrooms();
  // });

  $("#getContactsButton").click(function () {
    loadContacts();
  });

  $("#addContactButton").click(async function () {
    var name = $("#contactName").val();
    var id = $("#contactId").val();
    await addContact(name, id);
  });

  /**
   * Funktion die alle Chatrooms anzeigt
   */
  function listChatrooms() {
    $(currentChatrooms).each(async function (index, item) {
      item = await getChatroomFromCurrent(item.id);
      if ($("#ch_" + item.id).length === 0) {
        var div = await createChatroomDiv(item);
        if ($("#ch_" + item.id).length === 0) {
          $("#chatroomscontainer").append(div);
          if (item.id === currentChatroomID) {
            await OnChatroomInfoVisible(item.id, true);
            $("#chatroomTitle").html(item.name);
          }
          sortChatrooms();
        }
        $("#ch_" + item.id).click(async function () {
          await selectChatroom(item.id);
        });
      } else {
        if ($("#ch_n_" + item.id).html() !== item.name) {
          $("#ch_n_" + item.id).html(item.name);
          if (item.id === currentChatroomID) {
            await OnChatroomInfoVisible(item.id, true);
            $("#chatroomTitle").html(item.name);
          }
        }
      }
    });
  }

  /**
   * Funktion zum auswaehlen eines Chatrooms
   * @param id
   * Der betroffene Chatroom
   */
  async function selectChatroom(id) {
    $(".chatroom").each(function () {
      if (this.id !== id) {
        this.classList.remove("active");
      }
    });
    $("#ch_" + id).addClass("active");
    var item = await getChatroomFromCurrent(id);
    await setChatroom(item);
  }

  /**
   * Funktion zum erstellen eines Chatrooms
   */
  async function createChatroom1() {
    var name = $("#chatName").val();
    if (name && name.length > 0) {
      await createChatroom(name);
    }
  }

  $("#createChatButton").click(async function () {
    await createChatroom1();
  });

  var showChatroomInfos = false;
  function updateChatroomContent() {
    if (showChatroomInfos) {
      $("#messagesinnercontainer").hide();
      $("#chatroomInfo").show();
    } else {
      $("#chatroomInfo").hide();
      $("#messagesinnercontainer").show();
      scrollToBottom();
      //   $("#message").focus();
    }
  }
  function toggleChatroomInfos() {
    showChatroomInfos = !showChatroomInfos;
    updateChatroomContent();
  }

  function showMessages() {
    showChatroomInfos = false;
    updateChatroomContent();
  }

  $("#chatroomTitle").click(function () {
    toggleChatroomInfos();
  });

  //#region collapse navigation

  //#endregion

  /**
   * Login-Funktion mit Name und Passwort
   * @param name
   * Der Username
   * @param pwd
   * Das Passwort
   */
  async function login(name, pwd) {
    await $.ajax({
      type: "POST",
      url: domain + "/login",
      data: {
        userName: name,
        passphrase: pwd,
      },
      beforeSend: function () {},
      success: async function (html) {
        await onUserLoginSuccess(html);
        $("#userNameInput").val("");
        $("#passwordInput").val("");
      },
      error: function (html) {
        onLoginFailed();
      },
    });
  }

  /**
   * Login Funktion mit ApiKey
   * @param apiKey
   * Ein String
   */
  async function loginWithKey(apiKey) {
    await $.ajax({
      type: "POST",
      url: domain + "/login",
      data: {
        apiKey: apiKey,
      },
      beforeSend: function () {
        $("#memberinfo").html("loggin in...");
      },
      success: async function (html) {
        await onUserLoginSuccess(html);
      },
      error: function (html) {
        onLoginFailed();
      },
    });
  }

  /**
   * Wird ausgelöst, wenn beim Laden der Anwendung ein Fehler auftritt.
   */
  function onLoginFailed() {
    $("#errorMessage").text("Fehler beim Laden der Anwendung.");
    $("#errorMessage").show();
    $("#mainContent").hide();
    $("#loginModal").show();
  }

  /**
   * Setzt den Avatar in der View
   * @param avatar
   * Der betroffene Avatar
   */
  function setAvatar(avatar) {
    $("#userAvatar").attr("src", avatar);
  }

  /**
   * Funktion zum laden von Kontakten und Chatrooms. Blendet bei Erfolg Login-Modal aus und MainContent ein
   * @param user
   * Der betroffene User
   */
  async function onUserLoginSuccess(user) {
    $("#errorMessage").text("");
    $("#errorMessage").hide("");
    $("#contactsList").html("");
    $("#chatroomscontainer").html("");
    $("#messagesinnercontainer .msgGlass").remove();

    currentUser = user;
    setCookie("apiKey", currentUser.apiKey);

    $("#memberinfoAvatar").attr("src", user.avatar);
    $("#memberinfo").html("Id: " + user.id + "<br>" + "Name: " + user.userName);

    $("#apiKeyContainer").html(currentUser.apiKey);

    setAvatar(user.avatar);
    $("#registerModal").hide();
    $("#loginModal").hide();
    $("#mainContent").show();
    await getContacts();
    await loadChatrooms();
    enableRefresh();
  }

  /**
   * Registrier-Funktion erstellt neuen User in der Datenbank mit Namen, Passwort und Avatar
   * @param name
   * Der Name des User
   * @param pwd
   * Das Passwort des User
   * @param avatar
   * Der Avatar des User
   */
  async function register(name, pwd, avatar) {
    await $.ajax({
      type: "POST",
      url: domain + "/users",
      data: {
        userName: name,
        passphrase: pwd,
      },
      beforeSend: function () {
        $("#memberinfo").html("registrating...");
      },
      success: async function (html) {
        await onUserLoginSuccess(html);
        await updateAvatar(avatar);
      },
      error: function (html) {
        $("#memberinfo").html("error registrating in");
      },
    });
  }

  function scrollToBottom() {
    $("#messagescontainer").scrollTop(1000000000);
  }

  /**
   * Initialisierungs-Funktion, generiert einen Cookie + ApiKey, ruft Login-Funktion auf und blendet View ein
   */
  async function initialize() {
    var apiKey = getCookie("apiKey");
    if (apiKey) {
      await loginWithApiKey(apiKey);
    }

    if (!currentUser) {
      $("#mainContent").hide();
      $("#loginModal").show();
    }

    setFileListener();
  }

  initialize();

  /**
   * Funktion die Nachrichten in Chatrooms laedt, wenn es neue Nachrichten gibt
   * @returns
   * boolean Wert
   */
  async function refreshMessages() {
    if (!refreshing) {
      return;
    }
    if (currentChatrooms.length === 0) {
      $("#messagesinnercontainer").html("Keine Chats");
    } else {
      if ($(".msgGlass").length === 0) {
        $("#messagesLoader").show();
      }
    }
    await getChatrooms();
    if (!currentChatroomID) {
      if (currentChatrooms && currentChatrooms.length > 0) {
        await setChatroom(currentChatrooms[0]);
      }
    }

    var room = await getChatroomFromCurrent(currentChatroomID);
    if (room && room.messages) {
      if (!refreshing) {
        return;
      }
      $(room.messages).each(function (index, item) {
        if (
          $("#m_" + item.id).length === 0 &&
          item.chatroomId === currentChatroomID
        ) {
          $("#messagesinnercontainer").append(createMessageDiv(item));
          scrollToBottom();
        }
      });
    }

    $(currentChatrooms).each(async function (index, item) {
      var latestMessage = parseInt(new Date(item.latestMessage).getTime());
      var latestUserMessage = parseInt(
        new Date(item.latestUserMessage).getTime()
      );

      var hasNewMessages = latestMessage > latestUserMessage;
      if (item.id === currentChatroomID && $(".msg").length === 0) {
        hasNewMessages = true;
      }
      if (hasNewMessages) {
        if (!refreshing) {
          return;
        }
        if (item.id === currentChatroomID) {
          var result = await getMessages(item.id);
          if (result) {
            sortChatrooms();
          }
          scrollToBottom();
        } else {
          if ($("#ch_b_" + item.id).text().length === 0) {
            sortChatrooms();
            $("#ch_b_" + item.id).text("1");
            notify();
          }
        }
      }
    });
  }

  /**
   * Funktion zum Vergleichen von zwei Chatrooms
   * @param a
   * Erster Chatroom zum vergleichen
   * @param b
   * Zweiter Chatroom zum vergleichen
   * @returns
   * boolean Wert
   */
  function compareChatrooms(a, b) {
    var idA = parseInt(a.id.substring(3));
    var idB = parseInt(b.id.substring(3));
    var roomA = currentChatrooms.find((c) => c.id === idA);
    var roomB = currentChatrooms.find((c) => c.id === idB);
    if (!roomA || !roomB) {
      return false;
    }
    var latestMessageA = new Date(roomA.latestMessage).getTime();
    var latestMessageB = new Date(roomB.latestMessage).getTime();
    var result = latestMessageA > latestMessageB;
    return result;
  }

  /**
   * Funktion die Chatrooms sortiert
   */
  function sortChatrooms() {
    var rooms = $(".chatroom");
    $(rooms).detach();
    $(rooms).sort(compareChatrooms).appendTo("#chatroomscontainer");
  }

  /**
   * Aktualisiert Ansicht des Avatars
   */
  function setFileListener() {
    $("#memberAvatarSelect").on("change", async function () {
      await updateAvatar(this.files[0]);
      location.reload(true);
    });
    $("#sendImageButton").click(async function () {
      $("#hiddenSendImageButton").trigger("click");
    });

    $("#hiddenSendImageButton").on("change", async function () {
      var img = await toBase64(this.files[0]);
      var type = this.files[0].type.substring(0, 5);
      if (type === "image") {
        await sendMessage(img, imageType, currentChatroomID);
      }
    });
    //Fileupload auf leer setzen
    $("#avatarFile").val("");

    //Wenn Datei hochgeladen -> Dateiname anzeigen
    document
      .querySelector(".custom-file-input")
      .addEventListener("change", function (e) {
        var name = document.getElementById("avatarFile").files[0].name;
        var nextSibling = e.target.nextElementSibling;
        nextSibling.innerText = name;
      });
  }

  /**
   * Funktion die einen Kontakt aus einem Chatroom entfernt
   * @param chatroomId
   * Der betroffene Chatroom
   * @param contactId
   * Der betroffene Kontakt
   */
  async function removeContactFromChatroom(chatroomId, contactId) {
    await $.ajax({
      type: "POST",
      url: domain + "/members",
      crossDomain: true,
      data: {
        apiKey: currentUser.apiKey,
        chatroomId: chatroomId,
        contactId: contactId,
        action: "remove",
      },
      success: async function () {
        if (contactId === currentUser.id) {
          var currentChatroom = currentChatrooms.find(
            (c) => c.id === chatroomId
          );
          currentChatrooms.splice(currentChatrooms.indexOf(currentChatroom), 1);
          $("#ch_" + chatroomId).remove();
          if (currentChatrooms.length > 0) {
            await selectChatroom(currentChatrooms[0].id);
          }
        } else {
          await OnChatroomInfoVisible(chatroomId, true);
        }
      },
    });
  }

  $("#addUserToChatSubmit").click(async function () {
    var toAdd = [];
    $("#contactsToAddList .active").each(function (index, item) {
      var id = item.id.substring(4);
      toAdd.push(id);
    });
    await addContactsToChatroom(currentChatroomID, toAdd);
  });

  /**
   * Funktion die Kontakte zu einem Chatroom hinzufuegt
   * @param chatroomId
   * Der betroffene Chatroom
   * @param contactIds
   * Die betroffenen Kontakte
   */
  async function addContactsToChatroom(chatroomId, contactIds) {
    await $.ajax({
      type: "POST",
      url: domain + "/members",
      crossDomain: true,
      data: {
        apiKey: currentUser.apiKey,
        chatroomId: chatroomId,
        ids: JSON.stringify(contactIds),
        action: "add",
      },
      success: async function () {
        await OnChatroomInfoVisible(chatroomId, true);
      },
    });
  }

  /**
   * Funktion die aufgerufen wird, wenn ein weiterer Kontakt ueber die Infobox des Chatrooms hinzugefuegt werden soll
   * @param item
   * Das betroffene Kontakt
   * @returns
   * Das List Element
   */
  function createContactToAddElement(item) {
    var newli = document.createElement("li");
    newli.classList.add("list-group-item");
    newli.classList.add("contact");
    newli.classList.add("justify-content-between");
    newli.classList.add("align-items-center");
    newli.classList.add("rounded");
    newli.id = "c_a_" + item.id;

    $(newli).click(function () {
      if (this.classList.contains("active")) {
        this.classList.remove("active");
      } else {
        this.classList.add("active");
      }
    });
    var newimg = document.createElement("img");
    if (item.avatar && item.avatar != "default") {
      newimg.src = item.avatar;
    } else {
      newimg.src =
        "https://pbs.twimg.com/profile_images/746460305396371456/4QYRblQD.jpg";
    }

    newli.append(newimg);

    newli.append(item.userName + " " + "(" + item.id + ")");

    return newli;
  }

  //drawing canvas
  let drawing = false;
  let location;
  const canvas = document.querySelector("#canvas");
  const context = canvas.getContext("2d");

  function clearCanvas() {
    context.clearRect(0, 0, canvas.width, canvas.height);
  }

  $("#openCanvasModalButton").click(function () {
    clearCanvas();
  });

  $("#clearCanvasButton").click(function () {
    clearCanvas();
  });

  $("#sendCanvasButton").click(async function () {
    var dataURL = canvas.toDataURL();
    await sendMessage(dataURL, imageType, currentChatroomID);
    $("#canvasModal").modal("hide");
  });

  function startPosition(e) {
    drawing = true;
    draw(e);
  }

  function finishPosition() {
    drawing = false;
    context.beginPath();
  }

  function normalizeCanvasCoords(x, y) {
    var boundingBox = canvas.getBoundingClientRect();
    return {
      x: (x - boundingBox.left) * (canvas.width / boundingBox.width),
      y: (y - boundingBox.top) * (canvas.height / boundingBox.height),
    };
  }

  function draw(e) {
    var x = e.clientX;
    var y = e.clientY;
    location = normalizeCanvasCoords(e.clientX, e.clientY);
    x = location.x;
    y = location.y;
    if (!drawing) {
      return;
    }

    context.lineJoin = context.lineCap = "round";

    if (currentColor !== "transparent") {
      context.globalCompositeOperation = "source-over";
    } else {
      context.globalCompositeOperation = "destination-out";
      context.arc(x, y, 8, 0, Math.PI * 2, false);
      context.fill();
    }

    context.lineTo(x, y);
    context.stroke();
    context.beginPath();
    context.moveTo(x, y);
  }

  function drawTouch(e) {
    var touch = e.originalEvent.touches[0];
    var mouseEvent = new MouseEvent("mousemove", {
      clientX: touch.clientX,
      clientY: touch.clientY,
    });
    draw(mouseEvent);
  }

  canvas.addEventListener("mousedown", startPosition);
  canvas.addEventListener("mouseup", finishPosition);
  canvas.addEventListener("mousemove", draw);
  $(canvas).bind("touchmove", function (event) {
    drawTouch(event);
  });
  canvas.addEventListener("touchstart", startPosition);
  canvas.addEventListener("touchend", finishPosition);

  //   $("#canvasModal").modal("show");
  var palette = $(".drawPalette");
  var colors = ["#000", "#FFF", "#F00", "#0F0", "#00F", "#FF0", "#0FF", "#F0F"];

  var currentColor = document.createElement("div");
  currentColor.id = "currentPaletteColor";
  currentColor.className = "paletteColor btn";
  $(currentColor).css("background-color", context.strokeStyle);
  $(palette).append(currentColor);

  context.lineWidth = 8;

  var lineWidthSlider = document.createElement("input");
  lineWidthSlider.type = "range";
  lineWidthSlider.className = "form-range";
  lineWidthSlider.min = 1;
  lineWidthSlider.max = 20;
  lineWidthSlider.value = context.lineWidth;
  $(lineWidthSlider).change(function () {
    context.lineWidth = lineWidthSlider.value;
  });
  $(palette).append(lineWidthSlider);

  colors.forEach(function (item, index) {
    var col = document.createElement("div");
    col.className = "paletteColor btn";
    $(col).css("background-color", item);
    $(col).attr("data-color", index);

    $(col).click(function () {
      context.strokeStyle = item;
      $(currentColor).css("background-color", item);
    });

    $(palette).append(col);
  });
});
