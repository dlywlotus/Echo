import { Card } from '../ui/card'

const CustomCard = ({ children }: { children: React.ReactNode }) => {
    return (
        <Card className="w-full h-full flex flex-col lg:w-125 lg:h-187.5 p-4 justify-center items-center">
            {children}
        </Card>
    )
}

export default CustomCard