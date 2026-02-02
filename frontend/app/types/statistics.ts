export interface CategoryStat {
    name: string;
    value: number;
}

export interface DashboardStats {
    categoryStats: CategoryStat[];
    confidenceStats: number[]; 
}