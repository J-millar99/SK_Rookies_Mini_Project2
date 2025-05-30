import React, { useState } from "react";
import PhoneVerification from "../components/PhoneVerification";
import SignUpForm from "../components/SignUpForm";
import { Container, Typography } from '@mui/material';

const SignupPage = () => {
  const [userName, setUserName] = useState("");

  const handlePhoneVerified = (certifiedPhone, name) => {
    setUserName(name);
  };

  return (
    <div>
      <Container>
        <Typography variant="h4" sx={{ mt: 4 }}>회원가입 페이지</Typography>
      </Container>
      <PhoneVerification onSuccess={handlePhoneVerified} />
      {/* 본인인증이 완료된 이후에만 입력 필드 보여주는 로직도 추가 가능 */}
      <SignUpForm />
      {/* 인증 결과 반영 */}
      <div>본인인증된 이름: {userName}</div>
    </div>
  );
};

export default SignupPage;
