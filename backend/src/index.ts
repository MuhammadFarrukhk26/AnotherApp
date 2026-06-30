import express from 'express';
import cors from 'cors';
import dotenv from 'dotenv';
import bookingRoutes from './presentation/routes/booking.routes';
import { errorHandler } from './presentation/middleware/errorHandler';
import { connectDatabase } from './infrastructure/database/mongo';

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

// Establish database connection, then start Express server
async function startServer() {
  try {
    await connectDatabase();
    app.listen(PORT, () => {
      console.log(`[Server] Hazir Clean Architecture backend running on port ${PORT}`);
    });
  } catch (error) {
    console.error('[Server] Failed to start backend server:', error);
    process.exit(1);
  }
}

startServer();
