import mongoose from 'mongoose';

export async function connectDatabase(): Promise<void> {
  const mongodbUri = process.env.MONGODB_URI || 'mongodb://localhost:27017/hazir';
  
  try {
    console.log(`[Database] Connecting to MongoDB at ${mongodbUri.replace(/\/\/.*@/, '//<credentials>@')}...`);
    await mongoose.connect(mongodbUri);
    console.log('[Database] MongoDB connected successfully.');
  } catch (error) {
    console.error('[Database] MongoDB connection error:', error);
    process.exit(1);
  }
}
