import { useState } from 'react';
import MenuIcon from '@mui/icons-material/Menu';
import Menu from '@mui/material/Menu';
import MenuItem from '@mui/material/MenuItem';

const CategorySelect = ({ value, onChange, options, showIcon = true  }) => {
    const [anchorEl, setAnchorEl] = useState(null);

    // 현재 선택된 카테고리 라벨 찾기
    const selectedLabel = options.find(opt => opt.value === value)?.label || '카테고리 선택';
    const selectedIcon = options.find(opt => opt.value === value)?.icon;

    const handleOpen = (event) => {
        setAnchorEl(event.currentTarget);
    };

    const handleClose = () => {
        setAnchorEl(null);
    };

    const handleSelect = (optValue) => {
        if (value === optValue) {
            // 이미 선택된 값을 다시 누르면 해제
            onChange({ target: { value: "" } });
        } else {
            onChange({ target: { value: optValue } });
        }
        handleClose();
    };

    return (
        <div style={{ display: 'inline-block', paddingBottom: showIcon ? 12 : 0, width: showIcon ? undefined : '100%' }}>
            <button
                ref={el => (showIcon ? null : (CategorySelect._buttonRef = el))}
                style={{
                    width: showIcon ? 36 : '100%',
                    height: showIcon ? 40 : 60,
                    padding: showIcon ? 0 : '6px 16px',
                    marginTop: 3,
                    border: showIcon ? 'none' : '1.5px solid #bdbdbd',
                    borderRadius: showIcon ? 0 : 5,
                    background: 'none',
                    backgroundColor: 'none',
                    cursor: 'pointer',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    boxShadow: 'none',
                    transition: 'border-color 0.2s',
                }}
                type="button"
                aria-haspopup="true"
                aria-controls={anchorEl ? "category-menu" : undefined}
                aria-expanded={Boolean(anchorEl)}
                onClick={handleOpen}
                onMouseOver={e => { if (!showIcon) e.currentTarget.style.borderColor = '#1976d2'; }}
                onMouseOut={e => { if (!showIcon) e.currentTarget.style.borderColor = '#bdbdbd'; }}
            >
                {showIcon ? (
                    <MenuIcon fontSize="large" />
                ) : (
                    <span style={{ fontWeight: 600, fontSize: 16, color: value ? "#1976d2" : "#888", width: '100%', textAlign: 'center' }}>
                        {selectedLabel}
                    </span>
                )}
            </button>
            <Menu
                id="category-menu"
                anchorEl={anchorEl}
                open={Boolean(anchorEl)}
                onClose={handleClose}
                MenuListProps={{
                    'aria-labelledby': 'category-menu-button',
                }}
                PaperProps={{
                    style: showIcon ? {} : { minWidth: anchorEl ? anchorEl.offsetWidth : undefined }
                }}
            >
                <MenuItem
                    disabled
                    selected={!value}
                    sx={{
                        fontWeight: 600,
                        color: value ? "#1976d2" : "#888",
                        fontSize: 15,
                    }}
                >
                    {showIcon && selectedIcon ? <span style={{ marginRight: 8 }}>{selectedIcon}</span> : null}
                    {selectedLabel}
                </MenuItem>
                {options.map(opt => (
                    <MenuItem
                        key={opt.value}
                        selected={value === opt.value}
                        onClick={() => handleSelect(opt.value)}
                        sx={{
                            fontSize: 14,
                            color: value === opt.value ? "#1976d2" : "inherit",
                            fontWeight: value === opt.value ? 700 : 400,
                        }}
                    >
                        {showIcon && opt.icon ? <span style={{ marginRight: 8 }}>{opt.icon}</span> : null}
                        {opt.label}
                    </MenuItem>
                ))}
            </Menu>
        </div>
    );
};

export default CategorySelect;