const siteVersion = "2.2.0"; // site version is different from skript version

// ID Scroll
const links = document.querySelectorAll("div.item-wrapper");
const contents = document.querySelectorAll("#content")[0];

lastActiveSideElement = null;
navContents = document.getElementById("nav-contents");

if (contents) {
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
  });
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

// Auto hash scroll on page load
document.addEventListener('DOMContentLoaded', (e) => {
  const linkHash = window.location.hash.replace("#", "");
  if (linkHash != "") {
    setTimeout(() => {
      toggleSyntax(linkHash);
      offsetAnchor(null, linkHash)
    }, 30); // respect other search and link changers
  }
});

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


document.querySelectorAll("a").forEach((e) => {
  e.addEventListener("click", (event) => {
    let id = e.getAttribute("href").replace("#", "");
    if (id != "" && id != null) {
      offsetAnchor(event, id);
      toggleSyntax(id);
    }
  });
})
// Anchor correction </>

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
    copyToClipboard(e.href);
    showNotification("✅ Link copied successfully.")
  });
})
// Anchor click copy link </>

// <> New element click
document.querySelectorAll(".new-element").forEach((e) => {
  e.addEventListener("click", (event) => {
    searchNow("is:new");
  });
})

// New element click </>

// <> Search Bar
const versionComparePattern = /.*?(\d\.\d(?:\.\d|))(\+|-|).*/gi;
const versionPattern = / ?v(?:ersion|):(\d\.\d(?:\.\d|-(?:beta|alpha|dev)\d*|))(\+|-|)/gi;
const typePattern = / ?t(?:ype|):(condition|expression|type|effect|event|section|function)/gi;
const newPattern = / ?is:(new)/gi;
const resultsFoundText = "result(s) found";

function versionCompare(base, target) { // Return -1, 0, 1
  // console.log(base + " | " + target)
  base = base.replaceAll(versionComparePattern, "$1").replaceAll(/[^0-9]/gi, "");
  target = target.replaceAll(versionComparePattern, "$1").replaceAll(/[^0-9]/gi, "");
  // console.log(base + " || " + target)

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
    searchNow(linkParams.get("search").split("#")[0]) // anchor link sometimes appear after the search param so filter it
  }, 20) // Until searchBar is loaded
}

var content = document.getElementById("content");
if (content) {
  let isNewPage = location.href.includes("/new.html");
  content.insertAdjacentHTML('afterbegin', `<a id="search-icon" ${isNewPage ? 'class="search-icon-new"' : ""} title="Copy the search link."><img src="https://img.icons8.com/color/35/000000/search--v1.png"></a>`);
  content.insertAdjacentHTML('afterbegin', `<span><input id="search-bar" ${isNewPage ? 'class="search-bar-version"' : ""} type="text" placeholder="🔍 Search the docs" title="Available Filters:&#13;&#10;&#13;&#10;Version:   v:2.5.3 v:2.2+ v:2.4-&#13;&#10;Type:      t:expression t:condition etc.&#13;&#10;New:       is:new"><span id="search-bar-after" style="display: none;">0 ${resultsFoundText}</span></span>`);
  searchBar = document.getElementById("search-bar");
  searchIcon = document.getElementById("search-icon");

  if (isNewPage) {
    setTimeout(() => {
      let tags = []
      let options = "<select id='search-version' name='versions' id='versions' onchange='checkVersionFilter()'></select>"
      content.insertAdjacentHTML('afterbegin', `<span>${options}</span>`);
      options = document.getElementById("search-version");

      let savedTags = getCookie("skVersions").split(",");
      for (let i = 0; i < savedTags.length; i++) { // Append saved versions then check
        if (savedTags[i] != "") {
          let option = document.createElement('option')
          option.value = savedTags[i]
          option.textContent = "Since v" + savedTags[i]
          options.appendChild(option)
        }
      }
      if (savedTags && !linkParams.get("search") && !window.location.href.match(/.*?#.+/)) // Don't search for versions if the url has a search filter nor hash link
        searchNow(`v:${savedTags[0]}+`) // Auto search on load

      $.getJSON("https://api.github.com/repos/SkriptLang/Skript/tags?per_page=83&page=2", (data) => { // 83 and page 2 matters to filter dev branches (temporary)
        let isThereNew = false;
        for (let i = 0; i < data.length; i++) {
          let tag = data[i]["name"]
          // if (!(/.*(dev|beta|alpha).*/gi).test(tag))
          tags.push(tag.replaceAll(/(.*)-(dev|beta|alpha).*/gi, "$1"));
        }
        tags = [...new Set(tags)] // remove duplicates
        setCookie("skVersions", tags, 5);
        for (let i = 0; i < tags.length; i++) {
          if (savedTags.includes(tags[i])) // Only add unsaved versions
            continue

          isThereNew = true; // Marks that a new version was added to update the search bar

          let option = document.createElement('option')
          option.value = tags[i]
          option.textContent = "Since v" + tags[i]
          options.appendChild(option)
        }
        if (isThereNew && !linkParams.get("search"))
          searchNow(`v:${tags[0]}+`)
      })
    }, 20);
  }
} else {
  content = document.getElementById("content-no-docs")
}

// Copy search link
if (searchIcon) {
  searchIcon.addEventListener('click', (event) => {
    let link = window.location.href.split("?")[0] // link without search param
    link += `?search=${encodeURI(searchBar.value)}`
    copyToClipboard(link)
    showNotification("✅ Search link copied.")
  })
}

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
  searchValue = searchValue.replaceAll(/[^a-zA-Z0-9 ]/gi, ""); // Filter none alphabet and digits to avoid regex errors

  allElements.forEach((e) => {
    let patterns = document.querySelectorAll(`#${e.id} .item-details .skript-code-block`);
    for (let i = 0; i < patterns.length; i++) { // Search in the patterns for better results
      let pattern = patterns[i];
      let regex = new RegExp(searchValue, "gi")
      let name = document.querySelectorAll(`#${e.id} .item-title h1`)[0].textContent // Syntax Name
      let filtersFound = false;

      // Version check
      let versionFound;
      if (version != "") {
        versionFound = document.querySelectorAll(`#${e.id} .item-details:nth-child(2) td:nth-child(2)`)[0].textContent.includes(version);

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
        // console.log(filterTypeEl.textContent + " | " + filterType + " | " + filterTypeFound)
      }

      if (filterNewFound && versionFound && filterTypeFound)
        filtersFound = true

      if ((regex.test(pattern.textContent.replaceAll("[ ]", " ")) || regex.test(name) || searchValue == "") && filtersFound) { // Replacing '[ ]' will improve some searching cases such as 'off[ ]hand'
        pass = true
        break; // Performance
      }
    }

    // Filter
    let sideNavItem = document.querySelectorAll(`#nav-contents a[href="#${e.id}"]`); // Since we have new.html we need to loop this
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

// <> Dark Mode

// Auto load DarkMode from cookies
if (getCookie("darkMode") == "false") {
  content.insertAdjacentHTML('beforeend', `<img style="z-index: 99;" src="./assets/light-on.svg" id="theme-switch">`);
  document.body.setAttribute('data-theme', 'white')
} else {
  content.insertAdjacentHTML('beforeend', `<img style="z-index: 99;" src="./assets/light-off.svg" id="theme-switch">`);

  // Auto load from system theme
  // const darkThemeMq = window.matchMedia("(prefers-color-scheme: dark)");
  // if (darkThemeMq.matches) {
  //   document.body.removeAttribute('data-theme');
  // } else {
  //   document.body.setAttribute('data-theme', 'white')
  // }
}


setTimeout(() => {
  var themeSwitcher = document.getElementById('theme-switch');
  // console.log(themeSwitcher);
  themeSwitcher.addEventListener('click', (event) => {
    // console.log("1");
    if (document.body.getAttribute("data-theme") == null) {
      document.body.setAttribute('data-theme', 'white');
      event.target.src = "./assets/light-on.svg";
      setCookie("darkMode", "false", 99);
    } else {
      event.target.src = "./assets/light-off.svg";
      document.body.removeAttribute('data-theme');
      setCookie("darkMode", "true", 99);
    }
  });
}, 500); // For some reason this wouldn't work in index.html (only) unless I add some delay o.O
// Dark Mode <>

// <> Placeholders
const ghAPI = "https://api.github.com/repos/SkriptLang/Skript"
function replacePlaceholders(html) {
  let innerHTML = html.innerHTML;
  if (innerHTML.includes("${latest-version}")) {
    let lv = $.getJSON(ghAPI + "/releases?per_page=1", (data) => {
      html.innerHTML = html.innerHTML.replaceAll("${latest-version}", data[0]["tag_name"]);
    })
  }
  if (innerHTML.includes("${latest-version-changelog}")) {
    let lv = $.getJSON(ghAPI + "/releases?per_page=1", (data) => {
      html.innerHTML = html.innerHTML.replaceAll("${stable-version-changelog}", data[0]["body"]).replaceAll("\\r\\n", "<br>");
    })
  }

  if (innerHTML.includes("${stable-version}")) {
    let lv = $.getJSON(ghAPI + "/releases/latest", (data) => {
      html.innerHTML = html.innerHTML.replaceAll("${stable-version}", data["tag_name"]);
    })
  }
  if (innerHTML.includes("${stable-version-changelog}")) {
    let lv = $.getJSON(ghAPI + "/releases/latest", (data) => {
      html.innerHTML = html.innerHTML.replaceAll("${stable-version-changelog}", data["body"]).replaceAll("\\r\\n", "<br>");
    })
  }

  if (innerHTML.includes("${latest-issue-")) {
    let lv = $.getJSON(ghAPI + "/issues?per_page=1", (data) => {
      html.innerHTML = html.innerHTML.replaceAll("${latest-issue-user}", data[0]["user"]["login"]);
      html.innerHTML = html.innerHTML.replaceAll("${latest-issue-title}", data[0]["title"]);
      html.innerHTML = html.innerHTML.replaceAll("${latest-issue-date}", data[0]["created_at"]);
    })
  }

  if (innerHTML.includes("${latest-pull-")) {
    let lv = $.getJSON(ghAPI + "/pulls?per_page=1", (data) => {
      html.innerHTML = html.innerHTML.replaceAll("${latest-pull-user}", data[0]["user"]["login"]);
      html.innerHTML = html.innerHTML.replaceAll("${latest-pull-title}", data[0]["title"]);
      html.innerHTML = html.innerHTML.replaceAll("${latest-pull-date}", data[0]["created_at"]);
    })
  }

  if (innerHTML.includes("${contributors-size}")) {
    let lv = $.getJSON(ghAPI + "/contributors?per_page=500", (data) => {
      html.innerHTML = html.innerHTML.replaceAll("${contributors-size}", data.length);
    })
  }

  if (innerHTML.includes("${site-version}")) {
    html.innerHTML = html.innerHTML.replaceAll("${site-version}", siteVersion);
  }

  if (innerHTML.includes("${contributors-size}")) {
    let lv = $.getJSON(ghAPI + "/contributors?per_page=500", (data) => {
      html.innerHTML = html.innerHTML.replaceAll("${contributors-size}", data.length);
    })
  }
}
replacePlaceholders(document.querySelector("body"));
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

// <> Syntax Highlighting

// ORDER MATTERS!!
// All regexes must be sorrouneded with () to be able to use group 1 as the whole match since Js doesn't have group 0
// Example:     .+     = X
// Example:     (.+)     = ✓
const patterns = [ // [REGEX, CLASS]
  [/((?<!#)#(?!#).*)/gi, "sk-comment"], // Must be first, : must be before ::
  [/(\:|\:\:)/gi, "sk-var"],
  [/((?<!href=)\".+?\")/gi, "sk-string"], // before others to not edit non skript code
  [/\b(add|give|increase|set|to|from|make|remove( all| every|)|subtract|reduce|delete|clear|reset|send|broadcast|wait|halt|create|(dis)?enchant|shoot|rotate|reload|enable|(re)?start|teleport|feed|heal|hide|kick|(IP(-| )|un|)ban|break|launch|leash|force|message|close|show|reveal|cure|poison|spawn)(?=[ <])\b/gi, "sk-eff"],
  [/\b(on (?=.+\:))/gi, "sk-event"],
  [/\b((parse )?if|else if|else|(do )?while|loop(?!-)|return|continue( loop|)|at)\b/gi, "sk-cond"],
  [/\b((|all )player(s|)|victim|attacker|sender|loop-player|shooter|uuid of |'s uuid|(location of |'s location)|console)\b/gi, "sk-expr"],
  [/\b((loop|event)-\w+)\b/gi, "sk-loop-value"],
  [/\b(contains?|(has|have|is|was|were|are|does)(n't| not|)|can('t| ?not|))\b/gi, "sk-cond"],
  [/\b(command \/.+(?=.*?:))/gi, "sk-command"],
  [/(&lt;.+?&gt;)/gi, "sk-arg-type"],
  [/\b(true)\b/gi, "sk-true"],
  [/\b(stop( (the |)|)(trigger|server|loop|)|cancel|false)\b/gi, "sk-false"],
  [/({)/gi, "sk-var"],
  [/(})/gi, "sk-var"],
  [/(\w+?(?=\(.*?\)))/gi, "sk-function"],
  [/((\d+?(\.\d+?)? |a |)(|minecraft |mc |real |rl |irl )(tick|second|minute|hour|day)s?)/gi, "sk-timespan"],
  [/\b(now)\b/gi, "sk-timespan"],
]

function highlightElement(element) {

  let lines = element.innerHTML.split("<br>")

  for (let j = 0; j < lines.length; j++) {
    Loop2: for (let i = 0; i < patterns.length; i++) {
      let match;
      let regex = patterns[i][0];
      let oldLine = lines[j];
      // console.log(regex)

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
}
// Example Collapse </>