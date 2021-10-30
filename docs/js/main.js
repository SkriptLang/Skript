// ID Scroll
const links = document.querySelectorAll("div.item-wrapper");
const contents = document.querySelectorAll("#content")[0];

lastActive = null;

if (contents) {
  contents.addEventListener('scroll', (e) => {
    links.forEach((ha) => {
      const rect = ha.getBoundingClientRect();
      if (rect.top > 0 && rect.top < 150) {
        const location = window.location.toString().split("#")[0];
        history.replaceState(null, null, location + "#" + ha.id);
        
        if (lastActive != null) {
          lastActive.classList.remove("active-item");
        }
        
        lastActive = document.querySelectorAll(`#nav-contents a[href="#${ha.id}"]`)[0];
        if (lastActive != null) {
          lastActive.classList.add("active-item");
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

// <> Mobile anchor correction due to doubled size header)
function offsetAnchor(event, element) {
  if (window.innerWidth <= 768) {
    event.preventDefault();
    content = document.querySelectorAll("#content")[0];
    actualElement = document.getElementById(element.getAttribute("href").replace("#", ""));
    content.scroll(0, actualElement.offsetTop - 15);
  }
}

document.querySelectorAll("#nav-contents a").forEach((e) => {
  e.addEventListener("click", (event) => {
    offsetAnchor(event, e);
  });
})
// Mobile anchor correction </>

// <> Anchor click copy link
function copyToClipboard() {
  setTimeout(() => {
    var cb = document.body.appendChild(document.createElement("input"));
    cb.value = window.location.href;
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
    copyToClipboard();
    showNotification("✅ Link copied successfully.")
  });
})
// Anchor click copy link </>

// <> Search Bar
function versionCompare(base, target) { // Return -1, 0, 1
  base = base.replaceAll(/(\d\.\d(\.\d|)).*/gi, "$1").replaceAll(/[^0-9]/gi, ""); // Handle special chars and versions like -dev21 and filter non digits
  target = target.replaceAll(/(\d\.\d(\.\d|)).*/gi, "$1").replaceAll(/[^0-9]/gi, "");

  base = parseInt(base) < 100 ? parseInt(base) * 10 : parseInt(base); // convert ten's to hundred's to fix (2.5.1+ not reiggering 2.6 by converting 26 -> 260)
  target = parseInt(target) < 100 ? parseInt(target) * 10 : parseInt(target);


  if (target > base)
    return 1
  if (target == base)
    return 0
  if (target < base)
    return -1
}

var content = document.getElementById("content");
if (content) {
  content.insertAdjacentHTML('afterbegin', '<input id="search-bar" type="text" placeholder="🔍 Search the documents.. (filters: v:1.0[.0][+])">');
}

var searchBar = document.getElementById("search-bar");
if (searchBar) {
  searchBar.focus() // To easily search without the need to click
  searchBar.addEventListener('keydown', (event) => {
    setTimeout(() => { // Important to actually get the value after typing or deleting
      let allElements = document.querySelectorAll(".item-wrapper");
      let searchValue = searchBar.value;
      let count = 0; // Check if any matches found
      let pass;
      
      let version = searchValue.replaceAll(/v(?:ersion|)\:(\d\.\d(?:\.\d)?)\+?/gi, "$1").replaceAll(/[^0-9.]/gi, "");
      let versionAndUp = searchValue.replaceAll(/v(?:ersion|)\:\d\.\d(?:\.\d)?(\+?)/gi, "$1").replaceAll(/[^+]/g, "") == "+";
      searchValue = searchValue.replaceAll(/ ?v(ersion|)\:(\d\.\d(\.\d)?)\+?/gi, "") // Don't include filters in the search
      searchValue = searchValue.replaceAll(/( ){2,}/gi, " ") // Filter duplicate spaces

      searchValue = searchValue.replaceAll(/[^a-zA-Z0-9 ]/gi, ""); // Filter none alphabet and digits to avoid regex errors

      allElements.forEach((e) => {
        let patterns = document.querySelectorAll(`#${e.id} .item-details .skript-code-block`);
        for (let i = 0; i < patterns.length; i++) { // Search in the patterns for better results
          let pattern = patterns[i];
          // let regex = new RegExp("\\b" + searchValue + "\\b", "gi") // Makes live character update useless
          let regex = new RegExp(searchValue, "gi")
          let name = document.querySelectorAll(`#${e.id} .item-title h1`)[0].textContent // Syntax Name
          let versionFound;

          if (version != "") {
            versionFound = document.querySelectorAll(`#${e.id} .item-details:nth-child(2) td:nth-child(2)`)[0].textContent.includes(version);
            
            if (versionAndUp) {
              let versions = document.querySelectorAll(`#${e.id} .item-details:nth-child(2) td:nth-child(2)`)[0].textContent.split(",");
              for (const v in versions) { // split on ',' without space in case some version didn't have space and versionCompare will handle it
                if (versionCompare(version, versions[v]) == 1 == true) {
                  versionFound = true;
                  break; // Performance
                }
              }
            }
          } else {
            versionFound = true;
          }
          if ((regex.test(pattern.textContent) || regex.test(name) || searchValue == "") && versionFound) {
            pass = true
            break; // Performance
          }
        }

        // Filter
        let sideNavItem = document.querySelectorAll(`#nav-contents a[href="#${e.id}"]`)[0];
        if (pass) {
          e.style.display = null;
          if (sideNavItem)
            sideNavItem.style.display = null;
          count++;
        } else {
          e.style.display = "none";
          if (sideNavItem)
            sideNavItem.style.display = "none";
        }

        pass = false; // reset
      })

      if (count == 0) {
        if (document.getElementById("no-matches") == null)
        document.getElementById("content").insertAdjacentHTML('beforeend', '<p id="no-matches" style="text-align: center;">No matches found.</p>');
      } else {
        if (document.getElementById("no-matches") != null)
          document.getElementById("no-matches").remove();
      }
  
      count = 0; // reset
    }, 100); // Spam delay for better performance

  }); 
}
// Search Bar </>

// <> HighlightJS 
document.querySelectorAll('pre.code').forEach(el => { // Apply the code formatting on the same <pre> not the <code> inside to not break the styling
  hljs.highlightElement(el);
});
document.querySelectorAll('div .skript-code-block').forEach(el => { // This lags the docs pages due to the huge amount of elements being parsed, we can disable this if lag is so bad for some people or keep it because it looks AMAZING!
  hljs.highlightElement(el);
});
// HighlightJS </>

// <> Placeholders
function replacePlaceholders(html) {
  let innerHTML = html.innerHTML;
  if (innerHTML.includes("${latest-version}")) {
    let lv = $.getJSON("https://api.github.com/repos/SkriptLang/Skript/releases?per_page=1", (data) => {
      html.innerHTML = html.innerHTML.replaceAll("${latest-version}", data[0]["tag_name"]);
    })
  }
  if (innerHTML.includes("${contributors-size}")) {
    let lv = $.getJSON("https://api.github.com/repos/SkriptLang/Skript/contributors?per_page=500", (data) => {
      html.innerHTML = html.innerHTML.replaceAll("${contributors-size}", data.length);
    })
  }
}
replacePlaceholders(document.querySelector("body"));
// Placeholders </>

