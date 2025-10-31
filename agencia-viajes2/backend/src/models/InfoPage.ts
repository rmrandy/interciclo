import mongoose, { Schema, Document } from 'mongoose';

export interface IInfoPage extends Document {
  title: string;
  slug: string;
  description?: string;
  category?: string;
  hero?: {
    heading?: string;
    subheading?: string;
    image?: string;
  };
  content?: any;
  published: boolean;
  createdAt: Date;
  updatedAt: Date;
}

const InfoPageSchema = new Schema<IInfoPage>({
  title: { type: String, required: true },
  slug: { type: String, required: true, unique: true, index: true },
  description: { type: String },
  category: { type: String },
  hero: {
    heading: { type: String },
    subheading: { type: String },
    image: { type: String }
  },
  content: { type: Schema.Types.Mixed },
  published: { type: Boolean, default: true }
}, { timestamps: true });

export default mongoose.models.InfoPage || mongoose.model<IInfoPage>('InfoPage', InfoPageSchema);


