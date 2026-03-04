import mongoose from 'mongoose';


export class MongoDBClient {
    static async connect() {
        try {
            const uri = `${process.env.MONGODB_URI}/${process.env.MONGODB_NAME}?authSource=admin`

            await mongoose.connect(uri)

            console.log('MongoDB is connected');
        } catch (error) {
            console.error(`Error: ${error.message} `);
            process.exit(1);
        }
    }
}
