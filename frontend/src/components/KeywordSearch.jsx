import SearchIcon from '@mui/icons-material/Search';

const KeywordSearch = ({ value, onChange, onSearch }) => {
    const handleKeyDown = (e) => {
        if (e.key === 'Enter' && onSearch) {
            onSearch();
        }
    };

    return (
        <div style={{ display: 'flex', alignItems: 'center', width: 500, height: 40,
         background: '#fff', borderRadius: 20, border: '1px solid #ccc', overflow: 'hidden' }}>
            <input
                type="text"
                placeholder="키워드 검색"
                value={value}
                onChange={onChange}
                onKeyDown={handleKeyDown}
                style={{ flex: 1, border: 0, outline: 'none', padding: '0 12px',
                    height: '100%', fontSize: 16, background: 'transparent' }}
            />
            <button
                onClick={onSearch}
                style={{
                    width: 36,
                    height: 36,
                    minWidth: 36,
                    minHeight: 36,
                    border: 'none',
                    background: '#EA002C',
                    color: '#fff',
                    cursor: 'pointer',
                    borderRadius: '50%',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    marginRight: 2,
                }}
            >
                <SearchIcon />
            </button>
        </div>
    );
};

export default KeywordSearch;