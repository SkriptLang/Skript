let isCookiesAccepted = getCookie("cookies-accepted") === "true";

// <> Cookies
function setCookie(cname, cvalue, exdays, force = false) {
  if (!isCookiesAccepted && !force)
    return;

  const d = new Date();
  d.setTime(d.getTime() + (exdays * 24 * 60 * 60 * 1000));
  let expires = "expires=" + d.toUTCString();
  document.cookie = cname + "=" + cvalue + ";" + expires + ";path=/; SameSite=None; Secure";
}

function getCookie(cname) {
  let name = cname + "=";
  let ca = document.cookie.split(';');
  for (let i = 0; i < ca.length; i++) {
    let c = ca[i];
    while (c.charAt(0) == ' ') {
      c = c.substring(1);
    }
    if (c.indexOf(name) == 0) {
      return c.substring(name.length, c.length);
    }
  }
  return "";
}

// Cookies </>

// <> localStorage

/**
 * Set the value of local storage item 
 * @param {string} item id
 * @param {object} value 
 * @param {double} exdays time in days
 */
function setStorageItem(item, value, exdays, force = false) {
  if (!isCookiesAccepted && !force)
    return;
    
  const d = new Date();
  d.setTime(d.getTime() + (exdays * 24 * 60 * 60 * 1000));
  localStorage.setItem(item, value + "; " + d.getTime());
}

/**
 * Remove a local storage item
 * @param {string} item the id of the item 
 */
function removeStorageItem(item) {
  localStorage.removeItem(item)
}

/**
 * Get local storage item (value & time if has one)
 * @param {string} item the item id 
 * @param {boolean} noExpireationCheck whether to check for expiration time and remove the item 
 * @returns the item object
 */
function getStorageItem(item, noExpireationCheck = false) {
  let result = localStorage.getItem(item);
  if (!result)
    return null;
    
  if (!noExpireationCheck) {
    if (isStorageItemExpired(result)) {
      removeStorageItem(item);
      return null;
    }
  }
  result = result.split("; ")[0];
  return result;
}

/**
 * Get local storage item value after split at ';'
 * @param {string} item the id of th item
 * @returns the item value
 */
function getStorageItemValue(item) {
  let result = localStorage.getItem(item);
  if (!result)
    return null;
  return result.split("; ")[0];
}

/**
 * @param {string} string the value of the item not the item id (the value without splitting)
 * @returns the expiration date
 */
function getStorageItemExpiration(value) {
  let expires = localStorage.getItem(value).split("; ")[1];
  if (!expires) { // item with no expiration date
    return null;
  }
  return new Date(expires);
}

/**
 * 
 * @param string the value of the item not the item id (the value without splitting)
 * @returns whether expired or not
 */
function isStorageItemExpired(value) {
  let expires = parseInt(value.split("; ")[1]);
  if (!expires) { // item with no expiration date
    return null;
  }
  return new Date(expires) < new Date();
}

// localStorage </>



function getApiValue(element, placeholder, apiPathName, callback, noReplace = false) {
  let placeholderName = placeholder.replace("ghapi-", "");
  let cv = getStorageItem(placeholder); // cached value
  if (noReplace) {
    if (cv) {
      callback(cv, true);
    } else {
      $.getJSON(ghAPI + `/${apiPathName}`, (data) => {
        let value = callback(data, false);
        setStorageItem(placeholder, value, 0.2);
      })
    }
    return;
  }

  let innerHTML = element.innerHTML;
  if (innerHTML.includes(`\${${placeholderName}}`)) {
    if (cv) {
      element.innerHTML = element.innerHTML.replaceAll(`\${${placeholderName}}`, cv);
    } else {
      $.getJSON(ghAPI + `/${apiPathName}`, (data) => {
        let value = callback(data, false);
        element.innerHTML = element.innerHTML.replaceAll(`\${${placeholderName}}`, value);
        setStorageItem(placeholder, value, 0.2);
      })
    }
  }
}

// Copy texts/links to clipboard
function copyToClipboard(value) {
  setTimeout(() => {
    let cb = document.body.appendChild(document.createElement("input"));
    cb.value = value;
    cb.focus();
    cb.select();
    document.execCommand('copy');
    cb.parentNode.removeChild(cb);
  }, 50)
}

// Show notification
function showNotification(text, bgColor, color) {
  var noti = document.body.appendChild(document.createElement("span"));
  noti.id = "notification-box";

  setTimeout(() => {
    noti.textContent = text;
    if (bgColor)
      noti.styles.backgroundColor = bgColor;
    if (color)
      noti.styles.backgroundColor = color;
    noti.classList.add("activate-notification");
    setTimeout(() => {
      noti.classList.remove("activate-notification");
      setTimeout(() => {
        noti.parentNode.removeChild(noti);
      }, 200);
    }, 1500);
  }, 50);
}

// <> Magic Text (&k)
function getRandomChar() {
  chars = "ÂÃÉÊÐÑÙÚÛÜéêëãòóôēĔąĆćŇň1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ~!@#$%^&*()-=_+{}[";
  return chars.charAt(Math.floor(Math.random() * chars.length) + 1)
}

function magicTextGen(element) {
  var msg = element.textContent;
  var length = msg.length;

  setInterval(() => {
    var newMsg = "";
    for (i = 0; i <= length; i++) {
      newMsg += getRandomChar(msg.charAt(i));
    }
    element.textContent = newMsg;

  }, 30)
}

function renderMagicText() {
  document.querySelectorAll('.magic-text').forEach((e) => {
    magicTextGen(e);
  })
}
// Magic Text (&k) </>

// Replace Github placeholders
function replacePlaceholders(element) {
  let innerHTML = element.innerHTML;
  if (innerHTML.includes("${latest-version}")) {
    getApiValue(element, "ghapi-latest-version", "releases", (data) => {
      return data[0]["tag_name"];
    });
  }

  if (innerHTML.includes("${latest-version-changelog}")) {
    getApiValue(element, "ghapi-latest-version-changelog", "releases", (data) => {
      return data["body"].replaceAll("\\r\\n", "<br>");
    });
  }

  if (innerHTML.includes("${stable-version}")) {
    getApiValue(element, "ghapi-stable-version", "releases/latest", (data) => {
      return data["tag_name"];
    });
  }

  if (innerHTML.includes("${stable-version-changelog}")) {
    getApiValue(element, "ghapi-stable-version-changelog", "releases/latest", (data) => {
      return data["body"].replaceAll("\\r\\n", "<br>");
    });
  }

  if (innerHTML.includes("${latest-issue-")) {
    getApiValue(element, "ghapi-latest-issue-user", "issues?per_page=1", (data) => {
      return data[0]["user"]["login"];
    });
    getApiValue(element, "ghapi-latest-issue-title", "issues?per_page=1", (data) => {
      return data[0]["title"];
    });
    getApiValue(element, "ghapi-latest-issue-date", "issues?per_page=1", (data) => {
      return data[0]["created_at"];
    });
  }

  if (innerHTML.includes("${latest-pull-")) {
    getApiValue(element, "ghapi-latest-pull-user", "pulls?per_page=1", (data) => {
      return data[0]["user"]["login"];
    });
    getApiValue(element, "ghapi-latest-pull-title", "pulls?per_page=1", (data) => {
      return data[0]["title"];
    });
    getApiValue(element, "ghapi-latest-pull-date", "pulls?per_page=1", (data) => {
      return data[0]["created_at"];
    });
  }

  if (innerHTML.includes("${site-version}")) {
    element.innerHTML = element.innerHTML.replaceAll("${site-version}", siteVersion);
  }

  if (innerHTML.includes("${contributors-size}")) {
    getApiValue(element, "ghapi-contributors-size", "contributors?per_page=500", (data) => {
      return data.length;
    });
  }
}

// <> Syntax Highlighting

// ORDER MATTERS!!
// All regexes must be sorrouneded with () to be able to use group 1 as the whole match since Js doesn't have group 0
// Example:     .+     = X
// Example:     (.+)     = ✓
var patterns = []; // [REGEX, CLASS]

function registerSyntax(regexString, flags, clazz) {
  try {
    regex = new RegExp(regexString, flags);
    patterns.push([regex, clazz]);
  } catch (error) {
    console.warn(`Either your browser doesn't support this regex or the regex is incorrect (${regexString}):` + error);
  }
}

registerSyntax("((?<!#)#(?!#).*)", "gi", "sk-comment") // Must be first, : must be before ::
registerSyntax("(\\:|\\:\\:)", "gi", "sk-var")
registerSyntax("((?<!href=)\\\".+?\\\")", "gi", "sk-string") // before others to not edit non skript code
// registerSyntax("\\b(add|give|increase|set|make|remove( all| every|)|subtract|reduce|delete|clear|reset|send|broadcast|wait|halt|create|(dis)?enchant|shoot|rotate|reload|enable|(re)?start|teleport|feed|heal|hide|kick|(IP(-| )|un|)ban|break|launch|leash|force|message|close|show|reveal|cure|poison|spawn)(?=[ <])\\b", "gi", "sk-eff") // better to be off since it can't be much improved due to how current codes are made (can't detect \\s nor \\t)
registerSyntax("\\b(on (?=.+\\:))", "gi", "sk-event")
registerSyntax("\\b((parse )?if|else if|else|(do )?while|loop(?!-)|return|continue( loop|)|at)\\b", "gi", "sk-cond")
registerSyntax("\\b((|all )player(s|)|victim|attacker|sender|loop-player|shooter|uuid of |'s uuid|(location of |'s location)|console)\\b", "gi", "sk-expr")
registerSyntax("\\b((loop|event)-\\w+)\\b", "gi", "sk-loop-value")
registerSyntax("\\b(contains?|(has|have|is|was|were|are|does)(n't| not|)|can('t| ?not|))\\b", "gi", "sk-cond")
registerSyntax("\\b(command \\/.+(?=.*?:))", "gi", "sk-command")
registerSyntax("(&lt;.+?&gt;)", "gi", "sk-arg-type")
registerSyntax("\\b(true)\\b", "gi", "sk-true")
registerSyntax("\\b(stop( (the |)|)(trigger|server|loop|)|cancel( event)?|false)\\b", "gi", "sk-false")
registerSyntax("({|})", "gi", "sk-var")
registerSyntax("(\\w+?(?=\\(.*?\\)))", "gi", "sk-function")
registerSyntax("((\\d+?(\\.\\d+?)? |a |)(|minecraft |mc |real |rl |irl )(tick|second|minute|hour|day)s?)", "gi", "sk-timespan")
registerSyntax("\\b(now)\\b", "gi", "sk-timespan")

function highlightElement(element) {

  let lines = element.innerHTML.split("<br>")

  for (let j = 0; j < lines.length; j++) {
    Loop2: for (let i = 0; i < patterns.length; i++) {
      let match;
      let regex = patterns[i][0];
      let oldLine = lines[j];

      while ((match = regex.exec(oldLine)) != null) {
        lines[j] = lines[j].replaceAll(regex, `<span class='${patterns[i][1]}'>$1</span>`)
        if (regex.lastIndex == 0) // Break after it reaches the end of exec count to avoid inf loop
          continue Loop2;
      }
    }
  }
  element.innerHTML = lines.join("<br>")
}
// Syntax Highlighting </>

// Offset page's scroll - Used for anchor scroll correction
function offsetAnchor(event, id) { // event can be null
  let content = document.querySelector("#content");
  let element = document.getElementById(id);

  if (content && element) {
    if (event != null)
      event.preventDefault();
    content.scroll(0, element.offsetTop - 25); // Should be less than the margin in .item-wrapper so it doesn't show the part of the previous .item-wrapper
  }
}

// <> Toggle a specific doc element block

// Active syntax
var lastActiveSyntaxID;
function toggleSyntax(elementID) {
  let element = document.getElementById(elementID)
  if (!element)
    return

  if (lastActiveSyntaxID != null)
    document.getElementById(lastActiveSyntaxID).classList.remove("active-syntax");

  element.classList.add("active-syntax");
  lastActiveSyntaxID = elementID;
}

// Toggle a specific doc element block </>