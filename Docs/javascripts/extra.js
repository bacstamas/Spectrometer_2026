/* This script listens for MkDocs' internal page transitions */
app.document$.subscribe(function() {
  var links = document.querySelectorAll("a");
  links.forEach(function(link) {
    if (link.href.includes("JavaDocs/index.html")) {
      link.setAttribute("target", "_blank");
      link.setAttribute("rel", "noopener");
    }
  });
});