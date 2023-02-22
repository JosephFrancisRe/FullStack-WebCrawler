var imageList = document.querySelector('ul.images');
var logoList = document.querySelector('ul.logos');
var urlInput = document.querySelector('input[name=url]');
var imagesDoc = document.getElementById("images");
var logosDoc = document.getElementById("logos");

apiCallBack = function(xhr, callback) {
    if (xhr.readyState == XMLHttpRequest.DONE) {
        if (xhr.status != 200) {
            let message = xhr.status + ":" + xhr.statusText + ":" + xhr.responseText;
            alert(message);
            throw 'API call returned bad code: ' + xhr.status;
        }
        let response = xhr.responseText ? JSON.parse(xhr.responseText) : null;
        if (callback) {
            callback(response);
        }
    }
}

updateList = function(response) {
    imageList.innerHTML = '';
    logoList.innerHTML = '';
    var logoIdentifiers = ['.svg', 'logo', 'favicon'];
    for (var i = 0; i < response.length; i++) {
        if (response[i]) {
            var img = document.createElement("img");
            img.width = 200;
            img.src = response[i];
            console.log(response[i]);
            if (!logoIdentifiers.some(v => img.src.includes(v))) {
                imageList.appendChild(img);
            } else {
                logoList.appendChild(img);
            }
        }
    }
}

makeApiCall = function(url, method, obj, callback) {
    let xhr = new XMLHttpRequest();
    xhr.open(method, url);
    xhr.onreadystatechange = apiCallBack.bind(null, xhr, callback);
    xhr.send(obj ? obj instanceof FormData || obj.constructor == String ? obj : JSON.stringify(obj) : null);
}

document.querySelector('.lucky').addEventListener("click", function(event) {
    event.preventDefault();
    var verifiedSites = ['http://books.toscrape.com/', 'https://ashesofcreation.com/', 'https://www.ultimateyankees.com/', 'https://www.spacejam.com/1996/'];
    var randomIndex = Math.floor(Math.random() * verifiedSites.length);
    document.getElementById('url').value = verifiedSites[randomIndex];
    makeApiCall('/main?url=' + verifiedSites[randomIndex], 'POST', null, updateList);
});

document.querySelector('.submit').addEventListener("click", function(event) {
    event.preventDefault();
    if (urlInput.value === '' || urlInput.value === 'Test Images Easter Egg') {
        makeApiCall('/main?url=', 'POST', null, updateList);
        document.getElementById('url').value = 'Test Images Easter Egg';
    } else {
        makeApiCall('/main?url=' + urlInput.value, 'POST', null, updateList);
    }
});

document.querySelector('.reset').addEventListener("click", function(event) {
    event.preventDefault();
    document.querySelector('ul.images').innerHTML = '';
    document.querySelector('ul.logos').innerHTML = '';
    document.getElementById('url').value = '';
    makeApiCall('/main?url=', 'DELETE', null, null);
});