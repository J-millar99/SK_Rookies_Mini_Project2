import { Box, Button } from '@mui/material';

const PaginationBar = ({ page, totalPages, goToPage }) => (
  <Box
    component="footer"
    sx={{
      mt: 3,
      width: '100%',
      mx: 'auto',
      bgcolor: '#fff',
      borderTop: '1px solid #eee',
      py: 2,
      display: 'flex',
      justifyContent: 'center',
      alignItems: 'center',
      gap: 2,
      zIndex: 100,
    }}
  >
    <Button variant="outlined" size="small" disabled={page <= 0}
      onClick={() => goToPage(page - 1)}>
      이전
    </Button>
    <Box sx={{ display: 'flex', gap: 1 }}>
      {Array.from({ length: totalPages }).map((_, idx) => (
        <Button
          key={idx}
          variant={idx === page ? "contained" : "outlined"}
          size="small"
          onClick={() => goToPage(idx)}
          sx={{
            minWidth: 36,
            width: 36,
            height: 36,
            p: 0,
            bgcolor: idx === page ? 'primary.main' : '#fff',
            color: idx === page ? '#fff' : 'primary.main',
            borderColor: 'primary.main',
            fontWeight: idx === page ? 700 : 400,
            boxShadow: 'none',
            '&:hover': {
              bgcolor: idx === page ? 'primary.dark' : 'primary.light',
              color: idx === page ? '#fff' : 'primary.main',
              borderColor: 'primary.main',
            },
            transition: 'none',
          }}
          disabled={idx === page}
        >
          {idx + 1}
        </Button>
      ))}
    </Box>
    <Button variant="outlined" size="small" disabled={page >= totalPages - 1}
      onClick={() => goToPage(page + 1)}>
      다음
    </Button>
  </Box>
);

export default PaginationBar;
