function toggleSidebar() {
  if (mini) {
    console.log("opening sidebar");
    document.getElementById("mySidebar").style.width = "250px";
    document.getElementById("rightPart").style.marginLeft = "250px";
    $(".chatroom span").css("visibility", "visible");
    this.mini = false;
  } else {
    console.log("closing sidebar");
    document.getElementById("mySidebar").style.width = "54px";
    document.getElementById("rightPart").style.marginLeft = "54px";
    $(".chatroom span").css("visibility", "collapse");
    this.mini = true;
  }
}
