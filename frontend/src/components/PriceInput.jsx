const PriceInput = ({ value, onChange, placeholder }) => {
    const handleChange = e => {
        let val = e.target.value;
        // 숫자만 입력, 음수/소수점 방지
        if (/^\d*$/.test(val)) {
            // 0만 여러개면 0 하나로, 0으로 시작하면 앞의 0 모두 제거
            if (/^0+$/.test(val)) {
                val = '0';
            } else if (/^0\d+/.test(val)) {
                val = val.replace(/^0+/, '');
            }
            onChange(val);
        }
    };
    return (
        <input
            type="text"
            placeholder={placeholder}
            value={value}
            onChange={handleChange}
            style={{ width: 120, height: 30, border: 'solid 1px',borderRadius: 5 }}
        />
    );
};

export default PriceInput;