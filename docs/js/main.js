// ID Scroll
const links = document.querySelectorAll("div.item-warpper");
const contents = document.querySelectorAll("#content")[0];

lastActive = null;

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


// Active Tab
const pageLink = window.location.toString().replaceAll(/(.*)\/(.+?).html(.*)/gi, '$2');
if (pageLink === "" || pageLink == window.location.toString()) // home page - when there is no `.+?.html` pageLink will = windown.location due to current regex
  document.querySelectorAll('#global-navigation a[href="index.html"]')[0].classList.add("active-tab");
else
  document.querySelectorAll(`#global-navigation a[href="${pageLink}.html"]`)[0].classList.add("active-tab");


// No Left Panel
const noLeftPanel = document.querySelectorAll('#content.no-left-panel')[0];
if (noLeftPanel != null)
  document.querySelectorAll('#side-nav')[0].classList.add('no-left-panel');

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
