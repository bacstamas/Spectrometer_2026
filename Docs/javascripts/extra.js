document.addEventListener("DOMContentLoaded", function() {
    var links = document.querySelectorAll("a");
    for (var i = 0; i < links.length; i++) {
        // Check if the link contains 'JavaDocs' or matches the exact name
        if (links[i].href.includes("JavaDocs/index.html")) {
            links[i].target = "_blank";
            links[i].rel = "noopener";
        }
    }
});
