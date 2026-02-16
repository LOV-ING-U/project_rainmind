import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
    stages: [
        { duration: '20s', target: 5 },
        { duration: '20s', target: 10 },
        { duration: '20s', target: 30 },
        { duration: '20s', target: 50 },
        { duration: '20s', target: 100 }
    ],
    thresholds: {
        http_req_duration: ['p(95) < 800']
    },
};

const BASE_URL = __ENV.BASE_URL;
const PASSWORD = 12345678;

const START = 1;
const COUNT = 200;

export default function() {
    const userId = ;
    const nickname = `seed_${userId}`;

    const loginResponse = http.post(
        `${BASE_URL}/v1/auth/user/login`,
        JSON.stringify(
            {
                nickname,
                password: PASSWORD
            }
        ),
        {
            headers: {
                'Content-Type': 'application/json'
            }
        }
    );

    // 200 OK check
    check(loginResult, {
        'login 200 OK': (r) => r.status == 200,
        'token exist': (r) => !!r.json('token')
    });

    const token = loginResult.json('token');
    if(!token){
        sleep(1);
        return;
    }

    // 스케줄 생성
    const now = new Date();
    const startAt = now.toISOString();
    const endAt = new Date(now.getTime() + 60 * 60 * 1000).toISOString();

    const scheduleResponse = http.post(
        `${BASE_URL}/v1/schedules`,
        JSON.stringify({
            title: 'something',
            locationId: 1,
            startAt: startAt,
            endAt: endAt
        }),
        {
            headers: {
                'Content-Type': 'application/json',
                Authorization: `Bearer ${token}`
            }
        }
    )
}