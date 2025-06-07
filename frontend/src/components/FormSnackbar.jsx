import Snackbar from "@mui/material/Snackbar";
import Alert from "@mui/material/Alert";

export default function FormSnackbar({ open, message, onClose, severity = "info", duration = 3000 }) {
  return (
    <Snackbar
      open={open}
      autoHideDuration={duration}
      onClose={onClose}
      anchorOrigin={{ vertical: 'bottom', horizontal: 'center' }}
    >
      <Alert onClose={onClose} severity={severity} sx={{ width: '100%' }}>
        {message}
      </Alert>
    </Snackbar>
  );
}
