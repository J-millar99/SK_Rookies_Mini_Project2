import { useState } from 'react';
import MenuIcon from '@mui/icons-material/Menu';

const CategorySelect = ({ value, onChange, options }) => {
    const [open, setOpen] = useState(false);

    // 현재 선택된 카테고리 라벨 찾기
    const selectedLabel = options.find(opt => opt.value === value)?.label || '카테고리';

    return (
        <div
            style={{
                position: 'relative',
                display: 'inline-block',
                paddingBottom: 12 // 시각적 간격만 띄움
            }}
            onMouseEnter={() => setOpen(true)}
            onMouseLeave={() => setOpen(false)}
        >
            <button
                style={{
                    width: 36,
                    height: 36,
                    padding: 0,
                    marginTop: 3,
                    border: 'none',
                    background: 'none',
                    cursor: 'pointer',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    boxShadow: 'none',
                }}
                type="button"
            >
                <MenuIcon fontSize="large" />
            </button>
            {open && (
                <div
                    style={{
                        position: 'absolute',
                        top: '100%',
                        left: 0,
                        background: '#fff',
                        border: '1px solid #ccc',
                        borderRadius: 4,
                        boxShadow: '0 2px 8px rgba(0,0,0,0.1)',
                        zIndex: 100,
                        minWidth: 120,
                        width: 120,
                    }}
                    // 드롭다운 전체에 마우스 이벤트 적용
                    onMouseEnter={() => setOpen(true)}
                    onMouseLeave={() => setOpen(false)}
                >
                    <div
                        style={{
                            padding: 6,
                            fontSize: 14,
                            fontWeight: 600,
                            color: value ? '#1976d2' : '#888',
                            borderBottom: '1px solid #eee',
                            background: '#fafafa',
                            textAlign: 'center',
                            cursor: value ? 'pointer' : 'default',
                            userSelect: 'none'
                        }}
                        onClick={() => {
                            if (value) {
                                onChange({ target: { value: '' } });
                                setOpen(false);
                            }
                        }}
                    >
                        {selectedLabel}
                    </div>
                    {/* 구분선만 추가, 여백 최소화 */}
                    {/* <div style={{ height: 8 }} /> */}
                    {options.map(opt => (
                        <div
                            key={opt.value}
                            style={{
                                padding: 4,
                                fontSize: 13,
                                cursor: 'pointer',
                                background: value === opt.value ? '#f0f0f0' : '#fff',
                                textAlign: 'center'
                            }}
                            onClick={() => {
                                if (value === opt.value) {
                                    onChange({ target: { value: '' } });
                                } else {
                                    onChange({ target: { value: opt.value } });
                                }
                                setOpen(false);
                            }}
                        >
                            {opt.label}
                        </div>
                    ))}
                </div>
            )}
        </div>
    );
};

export default CategorySelect;