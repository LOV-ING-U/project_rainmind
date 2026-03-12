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
    // 로그인 시도
    const loginResult = http.post(
        `${BASE_URL}/v1/auth/user/login`,
        JSON.stringify({
            nickname: NICKNAME,
            password: PASSWORD
        }),
        {
            headers: {
                "Content-Type": "application/json"
            }
        }
    );

    // 정상 로그인 확인 & 토큰 추출
    check(loginResult, {
        "login 200 OK": (r) => r.status == 200
    });

    const token = loginResult.json("token");
    if(!token){
        console.log("no token", loginResult.body);
        return;
    }

    // 일정 생성해보기
    const schedule = {
        title: "학교종이땡땡땡",
        locationId: 1,
        startAt: new Date(Date.now() + 60 * 2 * 1000).toISOString(),
        endAt: new Date(Date.now() + 60 * 5 * 1000).toISOString()
    };

    const scheduleResponse = http.post(
        `${BASE_URL}/v1/schedules`,
        JSON.stringify(schedule),
        {
            headers: {
                "Content-Type": "application/json",
                Authorization: `Bearer ${token}`
            }
        }
    );

    const responseOk = check(scheduleResponse, {
        "schedule created 201": (r) => r.status == 201
    });

    if(!responseOk) console.log("cannot create schedule: ", scheduleResponse.status, scheduleResponse.body);

    sleep(1);
}