import { Card } from "./ui/card";

const PageCard = ({ children }: { children: React.ReactNode }) => {
    return (
        <Card className="h-full w-full bg-background lg:m-4 lg:max-h-187.5 lg:w-125">
            {children}
        </Card>
    );
};

export default PageCard;
