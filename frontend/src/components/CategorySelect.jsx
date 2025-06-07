import { useState } from 'react';
import MenuIcon from '@mui/icons-material/Menu';
import Menu from '@mui/material/Menu';
import MenuItem from '@mui/material/MenuItem';
import { Button, Box, Typography, useTheme } from '@mui/material';

const CategorySelect = ({ value, onChange, options, showIcon = true }) => {
  const [anchorEl, setAnchorEl] = useState(null);
  const theme = useTheme();

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
    <Box sx={{ display: 'inline-block', pb: showIcon ? 1.5 : 0, width: showIcon ? undefined : '100%' }}>
      <Button
        onClick={handleOpen}
        sx={{
          width: showIcon ? 40 : '100%',
          height: showIcon ? 40 : 48,
          minWidth: showIcon ? 40 : 120,
          px: showIcon ? 0 : 2,
          py: showIcon ? 0 : 1,
          mt: 0.5,
          border: 'none',
          borderRadius: showIcon ? 0 : 2,
          background: 'none',
          boxShadow: 'none',
          fontWeight: 600,
          fontSize: showIcon ? 20 : 16,
          color: '#000',
          justifyContent: 'center',
          textTransform: 'none',
        }}
        aria-haspopup="true"
        aria-controls={anchorEl ? "category-menu" : undefined}
        aria-expanded={Boolean(anchorEl)}
      >
        {showIcon ? (
          <MenuIcon fontSize="large" />
        ) : (
          <Typography
            sx={{
              fontWeight: 600,
              fontSize: 16,
              width: '100%',
            }}
          >
            {selectedLabel}
          </Typography>
        )}
      </Button>
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
            color: value ? theme.palette.info.main : '#000',
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
              color: value === opt.value ? theme.palette.info.main : "#000",
              fontWeight: value === opt.value ? 700 : 400,
            }}
          >
            {showIcon && opt.icon ? <span style={{ marginRight: 8 }}>{opt.icon}</span> : null}
            {opt.label}
          </MenuItem>
        ))}
      </Menu>
    </Box>
  );
};

export default CategorySelect;