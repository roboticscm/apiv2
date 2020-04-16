// const REDIRECT_URL = 'http://172.16.30.12:8080'
  // const REDIRECT_URL = 'http://192.168.10.3:8080'
  const REDIRECT_URL = 'http://localhost:8080';
  const REMEMBER_LOGIN_KEY = 'RememberLogin';
  const USERNAME_KEY = 'Username';

  const params = new URLSearchParams(window.location.search);
  if (params.get('clear')) {
    localStorage.removeItem(REMEMBER_LOGIN_KEY);
  }

  if (localStorage.getItem(REMEMBER_LOGIN_KEY)) {
    window.location = `${REDIRECT_URL}`;
  }

  function getBrowserID() {
    var nav = window.navigator;
    var screen = window.screen;
    var guid = nav.mimeTypes.length + '';
    guid += nav.userAgent.replace(/\D+/g, '');
    guid += nav.plugins.length;
    guid += screen.height || '';
    guid += screen.width || '';
    guid += screen.pixelDepth || '';

    return guid;
  }

  function buildSessionId(sessionId) {
    var index = sessionId.length / 2;
    return sessionId.slice(0, index) + getBrowserID() + sessionId.slice(index);
  }

  function resetPassword() {
    let username = document.getElementById('username').value.trim();

    if (username.length == 0) {
      alert('Username must be not Empty');
      document.getElementById('username').focus();
      return;
    }

    let request = new XMLHttpRequest();
    request.open('POST', '/api/sys/auth/reset-password');
    request.setRequestHeader('content-type', 'application/json');
    let obj = {
      username: username,
    };

    document.getElementById('progressBar').style.display = 'flex';
    request.onreadystatechange = function () {
      if (
        request.readyState === XMLHttpRequest.DONE &&
        request.status === 200
      ) {
        document.getElementById('progressBar').style.display = 'none';

        setTimeout(() => {
          alert(request.responseText);
        }, 50);
      } else {
        console.log(
          `Fail status ${request.status}  state ${request.readyState}`
        );
        if (
          request.readyState === XMLHttpRequest.DONE &&
          request.responseText
        ) {
          document.getElementById('progressBar').style.display = 'none';
          setTimeout(() => {
            alert(request.responseText);
          }, 50);
        }
      }
    };

    request.send(JSON.stringify(obj));
  }

  function login(event) {
    event.preventDefault();
    let username = document.getElementById('username').value.trim();
    let password = document.getElementById('password').value.trim();

    if (username.length == 0) {
      alert('Username must be not Empty');
      document.getElementById('username').focus();
      return;
    }

    if (password.length == 0) {
      alert('Password must be not Empty');
      document.getElementById('password').focus();
      return;
    }

    let request = new XMLHttpRequest();
    request.open('POST', '/api/sys/auth/login');
    request.setRequestHeader('content-type', 'application/json');
    let obj = {
      username: username,
      password: password,
    };

    request.onreadystatechange = function () {
      if (
        request.readyState === XMLHttpRequest.DONE &&
        request.status === 200
      ) {
        let loginResult = JSON.parse(request.responseText);
        if ('SUCCESS' === loginResult.loginResult) {
          localStorage.setItem(USERNAME_KEY, username);

          let chkRememberLogin = document.getElementById('rememberMe');
          if (chkRememberLogin.checked) {
            localStorage.setItem(REMEMBER_LOGIN_KEY, true);
          } else {
            localStorage.removeItem(REMEMBER_LOGIN_KEY);
          }
          // window.location=`${REDIRECT_URL}?sessionId=${encodeURIComponent(buildSessionId(loginResult.token))}&userId=${loginResult.userId}&localeLanguage=${loginResult.lastLocaleLanguage}&companyId=${loginResult.companyId}`
          window.location = `${REDIRECT_URL}?sessionId=${encodeURIComponent(
            buildSessionId(loginResult.token)
          )}&userId=${loginResult.userId}&localeLanguage=${
            loginResult.lastLocaleLanguage
          }&companyId=${loginResult.companyId}`;
        }
      } else if (
        request.readyState === XMLHttpRequest.DONE &&
        request.status === 400
      ) {
        let loginResult = JSON.parse(request.responseText);
        if ('WRONG_USERNAME' === loginResult.loginResult)
          alert('Wrong username');
        else if ('WRONG_PASSWORD' === loginResult.loginResult)
          alert('Wrong password');
        else if ('NOT_SET_COMPANY' === loginResult.loginResult)
          alert('Please setup your Company before login');
        else alert('Unkown error: ' + loginResult);
      } else {
        console.log(
          `Fail status ${request.status}  state ${request.readyState}`
        );
      }
    };

    request.send(JSON.stringify(obj));
  }

  function doRememberMe() {
    var display = document.getElementById('rememberMe').checked
      ? 'inline'
      : 'none';

    document.getElementById('periodOfTime').style.display = display;
  }

  function getQrcode() {
    let request = new XMLHttpRequest();
    request.open('POST', '/api/sys/auth/get-qrcode');
    request.setRequestHeader('content-type', 'application/json');

    return new Promise((resolve, reject) => {
      request.onreadystatechange = function () {
        if (
          request.readyState === XMLHttpRequest.DONE &&
          request.status === 200
        ) {
          resolve(request.responseText);
        } else if (request.status !== 200) {
        	reject(request.responseText);
        }
      };
      request.send();
    });
  }

  document.getElementById('username').value =
    localStorage.getItem(USERNAME_KEY) || '';