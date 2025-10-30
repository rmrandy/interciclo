import { createApp } from "vue";
import router from "./router";
import "./style.css";
import App from "./App.vue";

createApp(App).use(router).mount("#app");

// Analytics: enviar clics con sendBeacon sin bloquear la UI
(() => {
  const backend = `${window.location.protocol}//${window.location.hostname}:6060`;
  const endpoint = `${backend}/api/metrics/click`;
  const sessionId = (() => {
    const key = 'analytics_session_id';
    let v = localStorage.getItem(key);
    if (!v) { v = Math.random().toString(36).slice(2) + Date.now(); localStorage.setItem(key, v); }
    return v;
  })();

  function cssPath(el: Element | null): string {
    if (!el) return '';
    const path: string[] = [];
    let node: Element | null = el;
    while (node && node.nodeType === 1 && path.length < 8) {
      const name = node.nodeName.toLowerCase();
      let selector = name;
      if ((node as HTMLElement).id) { selector += `#${(node as HTMLElement).id}`; path.unshift(selector); break; }
      const className = (node as HTMLElement).className?.toString().trim().split(/\s+/).filter(Boolean).join('.') || '';
      if (className) selector += `.${className}`;
      const parent = node.parentElement;
      if (parent) {
        const siblings = Array.from(parent.children).filter(ch => ch.nodeName === node!.nodeName);
        if (siblings.length > 1) {
          const index = siblings.indexOf(node) + 1;
          selector += `:nth-of-type(${index})`;
        }
      }
      path.unshift(selector);
      node = parent;
    }
    return path.join(' > ');
  }

  window.addEventListener('click', (ev) => {
    try {
      const t = ev.target as HTMLElement;
      const payload: any = {
        userId: (() => { try { return JSON.parse(localStorage.getItem('user') || 'null')?.id || null; } catch { return null; } })(),
        sessionId,
        urlPath: window.location.pathname,
        fullUrl: window.location.href,
        pageTitle: document.title,
        elementTag: t?.tagName?.toLowerCase() || null,
        elementId: t?.id || null,
        elementClasses: t?.className?.toString() || null,
        textSnippet: (t?.innerText || '').trim().slice(0, 120),
        cssSelector: cssPath(t),
        x: (ev as MouseEvent).clientX,
        y: (ev as MouseEvent).clientY,
        vpW: window.innerWidth,
        vpH: window.innerHeight,
      };
      const blob = new Blob([JSON.stringify(payload)], { type: 'application/json' });
      const ok = (() => { try { return navigator.sendBeacon(endpoint, blob); } catch { return false; } })();
      if (!ok) {
        try {
          fetch(endpoint, { method: 'POST', body: JSON.stringify(payload), headers: { 'Content-Type': 'application/json' }, keepalive: true });
        } catch {}
      }
    } catch {}
  }, { capture: true });
})();
