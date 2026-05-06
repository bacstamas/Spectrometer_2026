// This specific hook is provided by MkDocs Material
// It runs every time the page or navigation changes
window.addEventListener("load", function() {
  const observer = new MutationObserver(() => {
    const links = document.querySelectorAll(".md-nav__link");
    links.forEach(link => {
      if (link.href.includes("JavaDocs/index.html")) {
        link.setAttribute("target", "_blank");
        link.setAttribute("rel", "noopener");
        // This stops the internal 'Instant Loading' from blocking the new tab
        link.onclick = function() {
          window.open(this.href, '_blank');
          return false;
        };
      }
    });
  });

  observer.observe(document.body, { childList: true, subtree: true });
});