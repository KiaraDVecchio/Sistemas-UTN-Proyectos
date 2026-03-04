class SiteFooter extends HTMLElement {
  async connectedCallback() {
    try {
      const res = await fetch('/partials/footer.html', { cache: 'no-cache' });
      this.innerHTML = await res.text();
    } catch {
      this.innerHTML = `<footer class="site-footer"><div class="footer-container"><div class="footer-col"><p class="muted">No se pudo cargar el pie de página.</p></div></div></footer>`;
    }
  }
}
customElements.define('site-footer', SiteFooter);
