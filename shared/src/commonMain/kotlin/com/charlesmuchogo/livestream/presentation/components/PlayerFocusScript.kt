package com.charlesmuchogo.livestream.presentation.components

const val PLAYER_FOCUS_SCRIPT = """
(function () {
  if (window.__lsFocusInstalled) return;
  window.__lsFocusInstalled = true;

  var FOCUS_CLASS = 'ls-player-focus';
  var STYLE_ID = 'ls-player-focus-style';
  var WATCH_MILLIS = 20000;
  var enabled = true;
  var focused = null;
  var reported = {};
  var BLOCKED_TEXT = /remove sandbox|sandbox attribute|sandboxed|disable (your )?ad.?block|adblock detected/i;

  function report(kind) {
    if (reported[kind]) return;
    reported[kind] = true;
    try {
      if (window.LivestreamBridge && window.LivestreamBridge.postMessage) {
        window.LivestreamBridge.postMessage(kind);
      } else if (window.webkit && window.webkit.messageHandlers && window.webkit.messageHandlers.LivestreamBridge) {
        window.webkit.messageHandlers.LivestreamBridge.postMessage(kind);
      }
    } catch (ignored) {}
  }

  function checkBlocked() {
    if (reported.playing || !document.body) return;
    var playing = Array.prototype.some.call(document.querySelectorAll('video'), function (v) { return !v.paused && v.readyState > 2; });
    if (!playing && BLOCKED_TEXT.test((document.body.innerText || '').slice(0, 2000))) report('blocked');
  }

  var UNMUTE_GUARD_MILLIS = 5000;
  var USER_TOUCH_MILLIS = 1500;
  var lastUserTouch = 0;

  function markUserTouch() { lastUserTouch = Date.now(); }
  document.addEventListener('pointerdown', markUserTouch, true);
  document.addEventListener('touchstart', markUserTouch, true);
  document.addEventListener('keydown', markUserTouch, true);

  function soundOn(video) {
    video.muted = false;
    video.defaultMuted = false;
    video.removeAttribute('muted');
    if (video.volume === 0) video.volume = 1;
  }

  function unmute(video) {
    if (video.__lsSoundStartedAt) return;
    video.__lsSoundStartedAt = Date.now();
    soundOn(video);
    if (video.paused) {
      var attempt = video.play();
      if (attempt && attempt.catch) {
        attempt.catch(function () {
          video.muted = true;
          video.play();
        });
      }
    }
  }

  document.addEventListener('playing', function (event) {
    report('playing');
    if (event.target && event.target.tagName === 'VIDEO') unmute(event.target);
  }, true);

  document.addEventListener('volumechange', function (event) {
    var video = event.target;
    if (!video || video.tagName !== 'VIDEO' || !video.__lsSoundStartedAt) return;
    var sinceStart = Date.now() - video.__lsSoundStartedAt;
    var userChanged = Date.now() - lastUserTouch < USER_TOUCH_MILLIS;
    if ((video.muted || video.volume === 0) && !userChanged && sinceStart < UNMUTE_GUARD_MILLIS) soundOn(video);
  }, true);

  var css =
    'html,body{background:#000!important;overflow:hidden!important;margin:0!important;padding:0!important}' +
    'body *{visibility:hidden!important}' +
    '.' + FOCUS_CLASS + ',.' + FOCUS_CLASS + ' *{visibility:visible!important}' +
    '.' + FOCUS_CLASS + '{position:fixed!important;left:0!important;top:0!important;right:auto!important;bottom:auto!important;' +
    'width:100vw!important;height:100vh!important;max-width:none!important;max-height:none!important;min-width:0!important;' +
    'margin:0!important;padding:0!important;border:0!important;border-radius:0!important;transform:none!important;' +
    'z-index:2147483647!important;background:#000!important}' +
    'video.' + FOCUS_CLASS + '{object-fit:contain!important}';

  function area(el) {
    var r = el.getBoundingClientRect();
    return Math.max(0, r.width) * Math.max(0, r.height);
  }

  function looksLikeClutter(el) {
    var text = (el.getAttribute('src') || '') + ' ' + (el.id || '') + ' ' + (typeof el.className === 'string' ? el.className : '');
    return /live_chat|chat|disqus|banner|doubleclick|googlesyndication|adserver|popunder|facebook\.com|twitter\.com|recaptcha/i.test(text);
  }

  function largest(list) {
    list.sort(function (a, b) { return area(b) - area(a); });
    return list.length ? list[0] : null;
  }

  function playerContainer(video) {
    var best = video;
    var videoArea = Math.max(area(video), 1);
    var node = video.parentElement;
    while (node && node !== document.body && node !== document.documentElement) {
      var nodeArea = area(node);
      if (nodeArea > videoArea * 1.6 || nodeArea === 0) break;
      best = node;
      node = node.parentElement;
    }
    return best;
  }

  function findPlayer() {
    var minWidth = Math.min(window.innerWidth * 0.35, 260);
    var videos = Array.prototype.slice.call(document.querySelectorAll('video')).filter(function (v) {
      return area(v) > 0 || v.readyState > 0;
    });
    if (videos.length) return playerContainer(largest(videos));

    var frames = Array.prototype.slice.call(document.querySelectorAll('iframe')).filter(function (f) {
      return !looksLikeClutter(f) && f.getBoundingClientRect().width >= minWidth;
    });
    var players = frames.filter(function (f) {
      return f.hasAttribute('allowfullscreen') || f.hasAttribute('webkitallowfullscreen') ||
        /embed|player|stream|live|watch|channel|ch=|tv/i.test(f.getAttribute('src') || '');
    });
    return largest(players.length ? players : frames);
  }

  function ensureStyle() {
    if (document.getElementById(STYLE_ID)) return;
    var style = document.createElement('style');
    style.id = STYLE_ID;
    style.textContent = css;
    (document.head || document.documentElement).appendChild(style);
  }

  function clear() {
    var style = document.getElementById(STYLE_ID);
    if (style) style.parentNode.removeChild(style);
    if (focused) focused.classList.remove(FOCUS_CLASS);
    focused = null;
  }

  function apply() {
    if (!enabled || !document.body) return;
    if (focused && document.contains(focused)) return;
    var player = findPlayer();
    if (!player) return;
    if (focused) focused.classList.remove(FOCUS_CLASS);
    focused = player;
    player.classList.add(FOCUS_CLASS);
    ensureStyle();
  }

  window.__lsSetPlayerFocus = function (on) {
    enabled = !!on;
    if (enabled) apply(); else clear();
  };

  function tick() {
    apply();
    checkBlocked();
  }

  function start() {
    tick();
    var observer = new MutationObserver(apply);
    observer.observe(document.documentElement, { childList: true, subtree: true });
    var timer = setInterval(tick, 1000);
    setTimeout(function () {
      observer.disconnect();
      clearInterval(timer);
    }, WATCH_MILLIS);
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', start);
  } else {
    start();
  }
})();
"""

const val BRIDGE_NAME = "LivestreamBridge"
const val BRIDGE_PLAYING = "playing"
const val BRIDGE_BLOCKED = "blocked"

fun playerFocusToggleScript(enabled: Boolean): String =
    "window.__lsSetPlayerFocus && window.__lsSetPlayerFocus($enabled);"
