import express from 'express';
import cors from 'cors';
import dotenv from 'dotenv';
import bookingRoutes from './presentation/routes/booking.routes';
import { errorHandler } from './presentation/middleware/errorHandler';

dotenv.config();

const app = express();
const PORT = process.env.PORT || 5000;

// Essential middlewares
app.use(cors());
app.use(express.json());

// API health endpoint
app.use('/health', (req, res) => {
  res.json({ status: 'OK', timestamp: new Date().toISOString() });
});

// Clean Architecture routes
app.use('/api/v1', bookingRoutes);

// Global Error Handler middleware
app.use(errorHandler);

app.listen(PORT, () => {
  console.log(`[Server] Hazir Clean Architecture backend running on port ${PORT}`);
});
