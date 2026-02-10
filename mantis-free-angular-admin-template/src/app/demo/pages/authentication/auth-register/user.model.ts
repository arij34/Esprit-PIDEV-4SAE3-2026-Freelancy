import { Subscription } from './subscription.model';

export enum Role {
    ADMIN = 'ADMIN',
    CLIENT = 'CLIENT',
    FREELANCER = 'FREELANCER'
}

export interface User {
    id?: number;
    firstName: string;
    lastName: string;
    email: string;
    password?: string;
    role?: Role;
    enabled?: boolean;
    createdAt?: string; // LocalDateTime
    subscription?: Subscription;
}

