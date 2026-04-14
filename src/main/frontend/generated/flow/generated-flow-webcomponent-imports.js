import { injectGlobalWebcomponentCss } from 'Frontend/generated/jar-resources/theme-util.js';

import '@vaadin/tooltip/src/vaadin-tooltip.js';
import '@vaadin/polymer-legacy-adapter/style-modules.js';
import '@vaadin/app-layout/src/vaadin-drawer-toggle.js';
import '@vaadin/side-nav/src/vaadin-side-nav.js';
import '@vaadin/icon/src/vaadin-icon.js';
import '@vaadin/side-nav/src/vaadin-side-nav-item.js';
import '@vaadin/icons/vaadin-iconset.js';
import '@vaadin/horizontal-layout/src/vaadin-horizontal-layout.js';
import '@vaadin/button/src/vaadin-button.js';
import 'Frontend/generated/jar-resources/disableOnClickFunctions.js';
import '@vaadin/vertical-layout/src/vaadin-vertical-layout.js';
import '@vaadin/app-layout/src/vaadin-app-layout.js';
import '@vaadin/login/src/vaadin-login-form.js';
import '@vaadin/common-frontend/ConnectionIndicator.js';
import '@vaadin/vaadin-lumo-styles/sizing.js';
import '@vaadin/vaadin-lumo-styles/spacing.js';
import '@vaadin/vaadin-lumo-styles/style.js';
import '@vaadin/vaadin-lumo-styles/vaadin-iconset.js';
import 'Frontend/generated/jar-resources/ReactRouterOutletElement.tsx';

const loadOnDemand = (key) => {
  const pending = [];
  if (key === 'ab9fc454ccf22ea1ca3fc7f3eae656f463a2d1d9b17fef0bebfd95cd6edcf199') {
    pending.push(import('./chunks/chunk-49469bdece74a2e271c190e87c717d3e8a7936821012b05c1a1c435e5591e601.js'));
  }
  if (key === '74cd876a24f79cb66370d619c9610df38c1d9b0d2a91af2d00a9f97da2ac2fce') {
    pending.push(import('./chunks/chunk-7595d6f7177eb798230f181a737f49da924562253d14aec0bcb2b5fa234eeb1b.js'));
  }
  if (key === 'e4fdf218145323afca7c8590cda8ebe69e9e7fa7abbbc990112c6ed006a81334') {
    pending.push(import('./chunks/chunk-be9d636f7dfffd01a5a9983e259816f6d787554043d7a0b79d3497f4b642d466.js'));
  }
  if (key === 'ea3fa9ed6f4a69a369f834d85ae97a31cbaf7cbd005814e764b4972c7db5bc8f') {
    pending.push(import('./chunks/chunk-64adbf0f065eecd327277ae9dd513597ac7317fe8601e8c9cdd5ba0b56dfb17f.js'));
  }
  if (key === '044dbba76fe5cbef92775151b3d42abe008fc83168ebaac6d07456204bf7a940') {
    pending.push(import('./chunks/chunk-6c2598be45d212d2f073c2240ba454063c6ee759480d40be8182d2ba783f32db.js'));
  }
  if (key === '82b0050f6463d77637fa62da3b468ff6dd714fdd790d5069e5783c320a29562c') {
    pending.push(import('./chunks/chunk-2fb5b9b4e6d98d7cb1b8560c2af023bc676928039ef87bcb0fa87f64a391ec35.js'));
  }
  if (key === 'a4027b23fbaa8b7db6852c21a4afeec04af7c1aa2b25663e1019d931a28fbfcf') {
    pending.push(import('./chunks/chunk-d4809e1f07d4a058dc9e3e1b265be5c9c7d792a37e9dcebcbaf4eb4a14573dcf.js'));
  }
  if (key === 'a63042cee19317496598e007e7a31d51516281eb5befb992ec3646065448bf3d') {
    pending.push(import('./chunks/chunk-21fdd832daa997c6a765a2a9daaff5312c0c1d0bac4c40a20235658caaac72dc.js'));
  }
  return Promise.all(pending);
}

window.Vaadin = window.Vaadin || {};
window.Vaadin.Flow = window.Vaadin.Flow || {};
window.Vaadin.Flow.loadOnDemand = loadOnDemand;
window.Vaadin.Flow.resetFocus = () => {
 let ae=document.activeElement;
 while(ae&&ae.shadowRoot) ae = ae.shadowRoot.activeElement;
 return !ae || ae.blur() || ae.focus() || true;
}