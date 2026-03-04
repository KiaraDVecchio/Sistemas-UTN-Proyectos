import { Skeleton } from "../../components/shared/Skeleton"

export const AlojamientoSkeleton = () => {
    return <div style={{ height: '80vh' }}>
        <div style={{ width: "100%", display: "flex", gap: "32px" }}>
            <div style={{ flexGrow: 2, display: "flex", flexDirection: "column", gap: "8px", justifyContent: "stretch", height: "100%" }}>
                <div style={{ width: "100%", display: "flex", flexDirection: "row", gap: "8px", height: "300px" }}>
                    <div style={{ flexGrow: 1 }}><Skeleton /></div>
                    <div style={{ flexGrow: 1 }}><Skeleton /></div>
                </div>
                <div style={{ width: "100%", display: "flex", flexDirection: "row", gap: "8px", height: "200px" }}>
                    <div style={{ flexGrow: 1 }}><Skeleton /></div>
                    <div style={{ flexGrow: 1 }}><Skeleton /></div>
                    <div style={{ flexGrow: 1 }}><Skeleton /></div>
                </div>
            </div>
            <div style={{ flexGrow: 1, display: "flex", flexDirection: "column", gap: "32px" }}>
                <div><Skeleton /></div>
                <div><Skeleton /></div>
                <div><Skeleton /></div>
                <div style={{ display: "flex", flexDirection: "row", gap: "16px", justifyContent: "stretch" }}>
                    <div style={{ borderRadius: "100%", height: "100%", aspectRatio: "1/1", overflow: "hidden" }}><Skeleton /></div>
                    <div style={{ flexGrow: 1 }}><Skeleton /></div>
                </div>

            </div>
        </div>
    </div>
}