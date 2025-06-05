import { Grid, Card, CardContent, Typography, Box, Chip, Divider } from '@mui/material';
import ProductCard from './ProductCard';

const CARD_WIDTH = 210;
const CARD_HEIGHT = 284;
const IMAGE_HEIGHT = 200;

const ProductsList = ({ products, formatTime, onProductClick }) => {
  return (
    <Grid
      container
      spacing={2}
      alignItems="stretch"
      justifyContent="flex-start"
      sx={{ width: "100%" }}
    >
      {products.map((product) => (
        <Grid
          item
          xs={12}
          sm={6}
          md={3}
          key={product.id}
          sx={{ display: 'flex', flexDirection: 'column', alignItems: 'center' }}
        >
          <div style={{ width: "100%" }} onClick={() => onProductClick && onProductClick(product.id)}>
            <ProductCard
              product={product}
              formatTime={formatTime}
            />
          </div>
        </Grid>
      ))}
    </Grid>
  );
};

export default ProductsList;