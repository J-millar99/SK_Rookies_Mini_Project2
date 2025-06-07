import React, { useEffect, useState } from "react";
import { Checkbox, FormControlLabel, Button, Alert } from "@mui/material";
import axios from "axios";
import { PORTONE_STORE_ID, PORTONE_CHANNEL_KEY, PORTONE_REDIRECT_URL } from "../utils/portone";
import FormSnackbar from "./FormSnackbar";

const PhoneVerification = ({ onSuccess }) => {
    const [verified, setVerified] = useState(false);
    const [serverMessage, setServerMessage] = useState(null); // 서버 응답 메시지 표시용

    // snackbar 상태 추가
    const [snackbar, setSnackbar] = useState({
        open: false,
        message: "",
        severity: "error",
    });

    useEffect(() => {
        // 포트원 SDK 로드
        const script = document.createElement("script");
        script.src = "https://cdn.iamport.kr/v1/iamport.js";
        script.async = true;
        script.onload = () => {
            if (window.IMP) {
                window.IMP.init(PORTONE_STORE_ID);
            }
        };
        document.body.appendChild(script);
    }, []);

    const handleVerification = () => {
        if (!window.IMP) {
            setSnackbar({
                open: true,
                message: "포트원 SDK가 아직 로드되지 않았습니다.",
                severity: "error",
            });
            return;
        }

        window.IMP.certification(
            {
                channelKey: PORTONE_CHANNEL_KEY,
                merchant_uid: `mid_${new Date().getTime()}`,
                m_redirect_url: PORTONE_REDIRECT_URL,
                popup: true,
            },
            function (rsp) {
                if (rsp.success) {
                    console.log("본인인증 성공", rsp);
                    // imp_uid 백엔드 서버로 전달
                    axios
                    .post("/api/verify_phone", { imp_uid: rsp.imp_uid }) // ← 백엔드 엔드포인트 수정 필요
                    .then((res) => {
                        const { success, reason, name } = res.data;

                        if (success) {
                            setVerified(true);
                            setServerMessage(null);
                            onSuccess?.(name); // 부모로 인증 결과 전달
                        } else {
                            setVerified(false);
                            setServerMessage(reason || "가입 조건을 만족하지 않습니다.");
                        }

                        console.log("서버 응답:", res.data);
                    })
                    .catch((err) => {
                        console.error("서버 전송 오류:", err);
                        setSnackbar({
                            open: true,
                            message: "서버로 인증 정보를 보내는 데 실패했습니다.",
                            severity: "error",
                        });
                    });
                } else {
                    setSnackbar({
                        open: true,
                        message: "본인인증 실패: " + rsp.error_msg,
                        severity: "error",
                    });
                }
            }
        );
    };

    return (
        <div>
            <FormControlLabel
                control={<Checkbox checked={verified} disabled />}
                label={verified ? "본인인증 완료" : "본인인증 필요"}
            />
            <Button variant="contained" onClick={handleVerification}>
                본인인증 시작
            </Button>
            {serverMessage && (
                <Alert severity="error" style={{ marginTop: "10px" }}>
                {serverMessage}
                </Alert>
            )}
            <FormSnackbar
                open={snackbar.open}
                message={snackbar.message}
                severity={snackbar.severity}
                onClose={() => setSnackbar({ ...snackbar, open: false })}
            />
        </div>
    );
};

export default PhoneVerification;