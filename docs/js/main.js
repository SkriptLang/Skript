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


// No Left Panel
for (e in {"content-no-docs": 0, "content": 1}) {
  let noLeftPanel = document.querySelectorAll(`#${e}.no-left-panel`)[0];
  if (noLeftPanel != null)
    document.querySelectorAll('#side-nav')[0].classList.add('no-left-panel');
}

// Magic Text
renderMagicText();

// Anchor scroll correction
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

// Open description/pattern links in same tab rather than scrolling because hash links uses search bar
document.querySelectorAll(".item-wrapper a:not(.link-icon)").forEach((e) => {
  e.addEventListener("click", (event) => {
    event.preventDefault();
    window.open(e.href);
  });
})


// Anchor click copy link
const currentPageLink = window.location.toString().replaceAll(/(.+?.html)(.*)/gi, '$1');
document.querySelectorAll(".item-title > a").forEach((e) => {
  e.addEventListener("click", (event) => {
    copyToClipboard(window.location.toString().split(/[?#]/g)[0] + "?search=#" + e.parentElement.parentElement.id);
    showNotification("✅ Link copied successfully.")
  });
})

// New element label click
document.querySelectorAll(".new-element").forEach((e) => {
  e.addEventListener("click", (event) => {
    searchNow("is:new");
  });
})

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
  requestedElementID = window.location.hash;
  if (requestedElementID != undefined && requestedElementID != "") {
    setTimeout(() => {
      searchNow(requestedElementID);
    }, 20) // Until searchBar is loaded
  }
}

var content = document.getElementById("content");
if (content) {
  let isNewPage = linkParams.get("isNew") != null;
  content.insertAdjacentHTML('afterbegin', `<a id="search-icon" ${isNewPage ? 'class="search-icon-new"' : ""} title="Copy the search link."><img style="width: 28px;" src="./assets/search.svg"></a>`);
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
	  let keywords = e.getAttribute("data-keywords")
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
           regex.test(desc) || regex.test(keywords) || "#" + id == searchValue || searchValue == "") && filtersFound) { // Replacing '[ ]' will improve some searching cases such as 'off[ ]hand'
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
// To save performance we use the class "placeholder" on the wrapper element of elements that contains the placeholder
// To only select those elements and replace their innerHTML
document.querySelectorAll(".placeholder").forEach(e => {
  replacePlaceholders(e);
});
// Placeholders </>

// <> Syntax Highlighting
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

// <> Cookies Accecpt
if (!isCookiesAccepted) {
  content.insertAdjacentHTML('beforeend', `<div id="cookies-bar"> <p> We use cookies and local storage to enhance your browsing experience and store github related statistics. By clicking "Accept", you consent to our use of cookies and local storage. </p><div style="padding: 10px; white-space: nowrap;"> <button id="cookies-accept">Accept</button> <button id="cookies-deny">Deny</button> </div></div>`);
}

let cookiesBar = document.querySelector("#cookies-bar");;
let cookiesAccept = document.querySelector("#cookies-accept");
let cookiesDeny = document.querySelector("#cookies-deny");
if (cookiesAccept && cookiesDeny) {
  cookiesAccept.addEventListener('click', () => {
    setCookie('cookies-accepted', true, 99, true);
    cookiesBar.remove();
  });
  cookiesDeny.addEventListener('click', () => {
    cookiesBar.remove();
  });
}
// Cookies Accecpt </>