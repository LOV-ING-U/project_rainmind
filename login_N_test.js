import http from "k6/http";
import { check, sleep } from 'k6';

export const options = {
    vus: 1,
    duration: "10s"
};

// 그대로 따라한다.
const BASE_URL = __ENV.BASE_URL || "http://172.25.48.1:8080";
const NICKNAME = __ENV.NICKNAME;
const PASSWORD = __ENV.PASSWORD;

export default function() {
    const payload = {
        nickname: NICKNAME,
        password: PASSWORD
    }

    const response = http.post(
        `${BASE_URL}/v1/auth/user/login`,
        JSON.stringify(payload),
        {
            headers: {
                "Content-Type": "application/json"
            }
        }
    );

    const isItOk = check(response, {
        "login 200": (r) => r.status == 200,
        "token exists": (r) => r.json("token") != null
    });

    if(!isItOk) console.log("login fail: status = ", response.status, response.body);

    sleep(1);
}