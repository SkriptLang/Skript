// 
// This file is made to fix theme flicker at load due to 'defer' in main.js loading
// 

// A quick fix to not use modules due to CORS on localhost
function setStorageItem(item, value, exdays) {
  const d = new Date();
  d.setTime(d.getTime() + (exdays * 24 * 60 * 60 * 1000));
  localStorage.setItem(item, value + "; " + d.toUTCString());
}

function removeStorageItem(item) {
  localStorage.removeItem(item)
}

function getStorageItem(item, noExpireationCheck = false) {
  let result = localStorage.getItem(item);
  if (!noExpireationCheck) {
    let expires;
    expires, result = result.split("; ")[1], result.split("; ")[0];
    if (expires) { // item with no expiration date
      if (new Date(expires) < new Date()) {
        removeStorageItem(item);
        return null;
      }
    }
  }
  return result;
}


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

// Auto load DarkMode from cookies
if (getCookie("darkMode") == "false") {
  document.body.setAttribute('data-theme', 'white')
  document.body.insertAdjacentHTML('beforeend', `<img style="z-index: 99;" src="./assets/light-on.svg" id="theme-switch">`);
} else {
  document.body.insertAdjacentHTML('beforeend', `<img style="z-index: 99;" src="./assets/light-off.svg" id="theme-switch">`);
  
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
  themeSwitcher.addEventListener('click', (event) => {
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