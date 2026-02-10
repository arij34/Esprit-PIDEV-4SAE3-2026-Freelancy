import { User } from './user.model';

export enum SubscriptionType {
    FREE = 'FREE',
    VIP = 'VIP'
}

export enum SubscriptionStatus {
    ACTIVE = 'ACTIVE',
    EXPIRED = 'EXPIRED'
}

export interface Subscription {
    id?: number;
    type: SubscriptionType;
    startDate: string; // LocalDate
    endDate: string; // LocalDate
    status: SubscriptionStatus;
    user?: User;
}
