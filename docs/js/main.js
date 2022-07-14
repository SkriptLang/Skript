const siteVersion = "2.2.0"; // site version is different from skript version
const ghAPI = "https://api.github.com/repos/SkriptLang/Skript";

// ID Scroll
const links = document.querySelectorAll("div.item-wrapper");
const contents = document.querySelector("#content");

lastActiveSideElement = null;
navContents = document.getElementById("nav-contents");

if (contents) {
  setTimeout(() => {
    contents.addEventListener('scroll', (e) => {
      links.forEach((ha) => {
        const rect = ha.getBoundingClientRect();
        if (rect.top > 0 && rect.top < 350) {
          // const location = window.location.toString().split("#")[0];
          // history.replaceState(null, null, location + "#" + ha.id); // Not needed since lastActiveSideElement + causes history spam
          
          if (lastActiveSideElement != null) {
            lastActiveSideElement.classList.remove("active-item");
        }
        
        lastActiveSideElement = document.querySelectorAll(`#nav-contents a[href="#${ha.id}"]`)[0];
        if (lastActiveSideElement != null) {
          lastActiveSideElement.classList.add("active-item");
          navContents.scroll(0, lastActiveSideElement.offsetTop - 100);
        }
      }
    });
  })}, 50); // respect auto hash scroll
}
  
  
// Active Tab
const pageLink = window.location.toString().replaceAll(/(.*)\/(.+?).html(.*)/gi, '$2');
if (pageLink === "" || pageLink == window.location.toString()) // home page - when there is no `.+?.html` pageLink will = windown.location due to current regex
  document.querySelectorAll('#global-navigation a[href="index.html"]')[0].classList.add("active-tab");
else
  document.querySelectorAll(`#global-navigation a[href="${pageLink}.html"]`)[0].classList.add("active-tab");

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

// No Left Panel
for (e in {"content-no-docs": 0, "content": 1}) {
  let noLeftPanel = document.querySelectorAll(`#${e}.no-left-panel`)[0];
  if (noLeftPanel != null)
    document.querySelectorAll('#side-nav')[0].classList.add('no-left-panel');
}

// <> Magic Text
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
renderMagicText();

// Magic Text </>

// <> Anchor scroll correction
function offsetAnchor(event, id) { // event can be null
  let content = document.querySelector("#content");
  let element = document.getElementById(id);

  if (content && element) {
    if (event != null)
      event.preventDefault();
    content.scroll(0, element.offsetTop - 25); // Should be less than the margin in .item-wrapper so it doesn't show the part of the previous .item-wrapper
  }
}

document.querySelectorAll(".link-icon").forEach((e) => {
  e.addEventListener("click", (event) => {
    let id = e.getAttribute("href").replace("#", "");
    if (id != "" && id != null) {
      // offsetAnchor(event, id);
      event.preventDefault();
      toggleSyntax(id);
    }
  });
})
// Anchor correction </>

// Open description/pattern links in same tab rather than scrolling because hash links uses search bar
document.querySelectorAll(".item-wrapper a").forEach((e) => {
  e.addEventListener("click", (event) => {
    event.preventDefault();
    window.open(e.href);
  });
})

// <> Anchor click copy link
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

const currentPageLink = window.location.toString().replaceAll(/(.+?.html)(.*)/gi, '$1');
document.querySelectorAll(".item-title > a").forEach((e) => {
  e.addEventListener("click", (event) => {
    copyToClipboard(window.location.toString().split(/[?#]/g)[0] + "?search=#" + e.parentElement.parentElement.id);
    showNotification("✅ Link copied successfully.")
  });
})
// Anchor click copy link </>

// <> New element label click
document.querySelectorAll(".new-element").forEach((e) => {
  e.addEventListener("click", (event) => {
    searchNow("is:new");
  });
})

// New element label click </>

// <> Search Bar
const versionComparePattern = /.*?(\d\.\d(?:\.\d|))(\+|-|).*/gi;
const versionPattern = / ?v(?:ersion|):(\d\.\d(?:\.\d|-(?:beta|alpha|dev)\d*|))(\+|-|)/gi;
const typePattern = / ?t(?:ype|):(condition|expression|type|effect|event|section|effectsection|function)/gi;
const newPattern = / ?is:(new)/gi;
const resultsFoundText = "result(s) found";

function versionCompare(base, target) { // Return -1, 0, 1
  base = base.replaceAll(versionComparePattern, "$1").replaceAll(/[^0-9]/gi, "");
  target = target.replaceAll(versionComparePattern, "$1").replaceAll(/[^0-9]/gi, "");

  base = parseInt(base) < 100 ? parseInt(base) * 10 : parseInt(base); // convert ten's to hundred's to fix (2.5.1+ not triggering 2.6 by converting 26 -> 260)
  target = parseInt(target) < 100 ? parseInt(target) * 10 : parseInt(target);

  if (target > base)
    return 1
  if (target == base)
    return 0
  if (target < base)
    return -1
}

var searchBar;
var searchIcon;

// Load search link
var linkParams = new URLSearchParams(window.location.href.replace("+", "%2B").split("?")[1]) // URLSearchParams decode '+' as space while encodeURI keeps + as is
if (linkParams && linkParams.get("search")) {
  setTimeout(() => {
    searchNow(linkParams.get("search")) // anchor link sometimes appear after the search param so filter it
  }, 20) // Until searchBar is loaded
} else {
  // Search the hash value if available
  requestedElementID = window.location.toString().replaceAll(/(.+?.html)(#.*)?/gi, '$2');
  if (requestedElementID != undefined && requestedElementID != "") {
    setTimeout(() => {
      searchNow(requestedElementID);
    }, 20) // Until searchBar is loaded
  }
}

var content = document.getElementById("content");
if (content) {
  let isNewPage = linkParams.get("isNew") != null;
  content.insertAdjacentHTML('afterbegin', `<a id="search-icon" ${isNewPage ? 'class="search-icon-new"' : ""} title="Copy the search link."><img src="https://img.icons8.com/color/35/000000/search--v1.png"></a>`);
  content.insertAdjacentHTML('afterbegin', `<span><input id="search-bar" ${isNewPage ? 'class="search-bar-version"' : ""} type="text" placeholder="Search the docs 🔍" title="Available Filters:&#13;&#10;&#13;&#10;Version:   v:2.5.3 v:2.2+ v:2.4-&#13;&#10;Type:      t:expression t:condition etc.&#13;&#10;New:       is:new"><span id="search-bar-after" style="display: none;">0 ${resultsFoundText}</span></span>`);
  searchBar = document.getElementById("search-bar");
  searchIcon = document.getElementById("search-icon");

  if (isNewPage) {
    let tags = []
    let options = "<select id='search-version' name='versions' id='versions' onchange='checkVersionFilter()'></select>"
    content.insertAdjacentHTML('afterbegin', `<span>${options}</span>`);
    options = document.getElementById("search-version");
    
    getApiValue(null, "skript-versions", "tags?per_page=83&page=2", (data, isCached) => { // 83 and page 2 matters to filter dev branches (temporary solution)
      if (isCached)
        data = data.split(",");

      for (let i = 0; i < data.length; i++) {
        let tag;
          if (isCached) {
            tag = data[i];
          } else {
            tag = data[i]["name"];
          }
          tags.push(tag.replaceAll(/(.*)-(dev|beta|alpha).*/gi, "$1"));
        }

      tags = [...new Set(tags)] // remove duplicates

      for (let i = 0; i < tags.length; i++) {
        let option = document.createElement('option')
        option.value = tags[i]
        option.textContent = "Since v" + tags[i]
        options.appendChild(option)
      }

      if (!linkParams.get("search") && !window.location.href.match(/.*?#.+/))
        searchNow(`v:${tags[0]}+`)

      return tags;
    }, true)
      
  }
} else {
  content = document.getElementById("content-no-docs")
}

// Copy search link
if (searchIcon) {
  searchIcon.addEventListener('click', (event) => {
    let link = window.location.href.split(/[?#]/g)[0] // link without search param
    link += `?search=${encodeURI(searchBar.value)}`
    copyToClipboard(link)
    showNotification("✅ Search link copied.")
  })
}

// Used when selecting a version from the dropdown
function checkVersionFilter() {
  let el = document.getElementById("search-version")
  if (el) {
    searchNow(`v:${el.value}+`)
  }
}

function searchNow(value = "") {
  if (value != "") // Update searchBar value
    searchBar.value = value;

  let allElements = document.querySelectorAll(".item-wrapper");
  let searchValue = searchBar.value;
  let count = 0; // Check if any matches found
  let pass;

  // version
  let version = "";
  let versionAndUp = false;
  let versionAndDown = false;
  if (searchValue.match(versionPattern)) {
    let verExec = versionPattern.exec(searchValue);
    version = verExec[1];
    if (verExec.length > 2) {
      versionAndUp = verExec[2] == "+" == true;
      versionAndDown = verExec[2] == "-" == true;
    }
    searchValue = searchValue.replaceAll(versionPattern, "") // Don't include filters in the search
  }

  // Type
  let filterType;
  if (searchValue.match(typePattern)) {
    filterType = typePattern.exec(searchValue)[1];
    searchValue = searchValue.replaceAll(typePattern, "")
  }

  // News
  let filterNew;
  if (searchValue.match(newPattern)) {
    filterNew = newPattern.exec(searchValue)[1] == "new";
    searchValue = searchValue.replaceAll(newPattern, "")
  }

  searchValue = searchValue.replaceAll(/( ){2,}/gi, " ") // Filter duplicate spaces
  searchValue = searchValue.replaceAll(/[^a-zA-Z0-9 #_]/gi, ""); // Filter none alphabet and digits to avoid regex errors

  allElements.forEach((e) => {
    let patterns = document.querySelectorAll(`#${e.id} .item-details .skript-code-block`);
    for (let i = 0; i < patterns.length; i++) { // Search in the patterns for better results
      let pattern = patterns[i];
      let regex = new RegExp(searchValue, "gi")
      let name = document.querySelectorAll(`#${e.id} .item-title h1`)[0].textContent // Syntax Name
      let desc = document.querySelectorAll(`#${e.id} .item-description`)[0].textContent // Syntax Desc
      let id = e.id // Syntax ID
      let filtersFound = false;

      // Version check
      let versionFound;
      if (version != "") {
        versionFound = versionCompare(version, document.querySelectorAll(`#${e.id} .item-details:nth-child(2) td:nth-child(2)`)[0].textContent) == 0;

        if (versionAndUp || versionAndDown) {
          let versions = document.querySelectorAll(`#${e.id} .item-details:nth-child(2) td:nth-child(2)`)[0].textContent.split(",");
          for (const v in versions) { // split on ',' without space in case some version didn't have space and versionCompare will handle it
            if (versionAndUp) {
              if (versionCompare(version, versions[v]) == 1) {
                versionFound = true;
                break; // Performance
              }
            } else if (versionAndDown) {
              if (versionCompare(version, versions[v]) == -1) {
                versionFound = true;
                break; // Performance
              }
            }
          }
        }
      } else {
        versionFound = true;
      }

      let filterNewFound = true;
      if (filterNew) {
        filterNewFound = document.querySelector(`#${e.id} .item-title .new-element`) != null
      }

      let filterTypeFound = true;
      let filterTypeEl = document.querySelector(`#${e.id} .item-title .item-type`);
      if (filterType) {
        filterTypeFound = filterType.toLowerCase() === filterTypeEl.textContent.toLowerCase()
      }

      if (filterNewFound && versionFound && filterTypeFound)
        filtersFound = true

      if ((regex.test(pattern.textContent.replaceAll("[ ]", " ")) || regex.test(name) ||
           regex.test(desc) || "#" + id == searchValue || searchValue == "") && filtersFound) { // Replacing '[ ]' will improve some searching cases such as 'off[ ]hand'
        pass = true
        break; // Performance
      }
    }

    // Filter
    let sideNavItem = document.querySelectorAll(`#nav-contents a[href="#${e.id}"]`); // Since we have new addition filter we need to loop this
    if (pass) {
      e.style.display = null;
      if (sideNavItem)
        sideNavItem.forEach(e => {
          e.style.display = null;
        })
      count++;
    } else {
      e.style.display = "none";
      if (sideNavItem)
        sideNavItem.forEach(e => {
          e.style.display = "none";
        })
    }

    pass = false; // Reset
  })

  searchResultBox = document.getElementById("search-bar-after");
  if (count > 0 && (version != "" || searchValue != "" || filterType || filterNew)) {
    searchResultBox.textContent = `${count} ${resultsFoundText}`
    searchResultBox.style.display = null;
  } else {
    searchResultBox.style.display = "none";
  }

  if (count == 0) {
    if (document.getElementById("no-matches") == null)
    document.getElementById("content").insertAdjacentHTML('beforeend', '<p id="no-matches" style="text-align: center;">No matches found.</p>');
  } else {
    if (document.getElementById("no-matches") != null)
      document.getElementById("no-matches").remove();
  }

  count = 0; // reset
}

if (searchBar) {
  searchBar.focus() // To easily search after page loading without the need to click
  searchBar.addEventListener('keydown', (event) => {
    setTimeout(() => { // Important to actually get the value after typing or deleting + better performance
      searchNow();
    }, 100);
  });
}
// Search Bar </>

// <> Placeholders

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

// To save performance we use the class "placeholder" on the wrapper element of elements that contains the placeholder
// To only select those elements and replace their innerHTML
document.querySelectorAll(".placeholder").forEach(e => {
  replacePlaceholders(e);
});
// Placeholders </>

// <> Cookies
function setCookie(cname, cvalue, exdays) {
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
function setStorageItem(item, value, exdays) {
  const d = new Date();
  d.setTime(d.getTime() + (exdays * 24 * 60 * 60 * 1000));
  localStorage.setItem(item, value + "; " + d.toUTCString());
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
    result = result.split("; ")[0];
    if (isStorageItemExpired(item)) {
      removeStorageItem(item);
      return null;
    }
  }
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
  let expires = value.split("; ")[1];
  if (!expires) { // item with no expiration date
    return null;
  }
  return new Date(expires) < new Date();
}

// localStorage </>

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

document.addEventListener("DOMContentLoaded", function (event) {
  setTimeout(() => {
    document.querySelectorAll('.item-examples .skript-code-block').forEach(el => {
      highlightElement(el);
    });
    document.querySelectorAll('pre code').forEach(el => {
      highlightElement(el);
    });
    document.querySelectorAll('.box.skript-code-block').forEach(el => {
      highlightElement(el);
    });
  }, 100);
});
// Syntax Highlighting </>


// <> Example Collapse
var examples = document.querySelectorAll(".item-examples p");
if (examples) {
  setTimeout(() => {
    examples.forEach(e => {
      let pElement = e;
      let divElement = e.parentElement.children[1];
      pElement.addEventListener("click", ev => {
        if (pElement.classList.contains("example-details-opened")) {
          pElement.classList.remove("example-details-opened");
          pElement.classList.add("example-details-closed");
          divElement.style.display = "none";
        } else {
          pElement.classList.remove("example-details-closed");
          pElement.classList.add("example-details-opened");
          divElement.style.display = "block";
        }
      })
    })
  }, 50)
}
// Example Collapse </>