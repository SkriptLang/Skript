// 
// This file is made to fix theme flicker at load due to 'defer' in main.js loading
// 

// Auto load DarkMode from cookies
if (getCookie("darkMode") == "false") {
  document.body.setAttribute('data-theme', 'white')
  document.body.insertAdjacentHTML('beforeend', `<img style="z-index: 99;" src="./assets/light-on.svg" id="theme-switch">`);
} else {
  document.body.insertAdjacentHTML('beforeend', `<img style="z-index: 99;" src="./assets/light-off.svg" id="theme-switch">`);
  
  // Auto load from system theme
  // const darkThemeMq = window.matchMedia("(prefers-color-scheme: dark)");
  // if (darkThemeMq.matches) {
  //     document.body.removeAttribute('data-theme');
  //   } else {
  //       document.body.setAttribute('data-theme', 'white')
  //     }
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
}, 500); // For some reason this wouldn't work in index.html (only) unless some delay is added o.O