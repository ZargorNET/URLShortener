class Alert {
    alert: HTMLElement;
    enabled = true;

    constructor(alertDiv: HTMLElement) {
        this.alert = alertDiv;
        this.toggle();
    }

    public view(txt: string) {
        if (txt != "")
            this.alert.innerText = txt;
        if (!this.enabled)
            this.toggle();
    }

    public hide() {
        if (this.enabled)
            this.toggle();
    }

    private toggle() {
        if (this.enabled)
            this.alert.classList.add("d-none");
        else
            this.alert.classList.remove("d-none");
        this.enabled = !this.enabled;
    }
}

const alertDanger: Alert = new Alert(document.getElementById("alert"));
const alertSuccess: Alert = new Alert(document.getElementById("success"));
const alertSuccessUrl: HTMLElement = document.getElementById("url-text");
const url: HTMLInputElement = document.getElementById("url-input") as HTMLInputElement;
const auth: HTMLInputElement = document.getElementById("auth-pwd") as HTMLInputElement;
var sending = false;

function send() {
    if (sending)
        return;
    sending = true;

    alertDanger.hide();
    alertSuccess.hide();
    const req = new XMLHttpRequest();
    req.onreadystatechange = () => {
        if (req.readyState == 4) {
            if (req.status == 200) {
                alertSuccessUrl.innerText = req.responseText;
                alertSuccess.view("");
            } else if (req.status == 400) {  //WHEN BAD_REQUEST
                alertDanger.view(req.responseText);
            } else {
                alertDanger.view("Unknown error! Server responded " + req.statusText + " (" + req.status + "). Message: " + req.responseText);
            }
            sending = false;
        }
    };

    const data = new FormData();
    data.append("url", encodeURI(url.value));
    data.append("auth", auth.value);

    req.open("POST", "/shorten", true);
    req.send(data);
}

function copyUrl() {
    const copyText = document.getElementById("url-text");
    const textArea = document.createElement("textarea");
    textArea.value = copyText.textContent;
    document.body.appendChild(textArea);
    textArea.select();
    document.execCommand("Copy");
    textArea.remove();
    alert("URL copied!");
}