const domain = "https://lehsetreff.de";

$(document).ready(function () {
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
        $("#message").text("Hello " + role + "!");
      },
      error: function (err) {
        $("#message").text(err);
      },
    });
  }
});
